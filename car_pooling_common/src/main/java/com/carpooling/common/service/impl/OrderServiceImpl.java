package com.carpooling.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.exception.OrderVerifyException;
import com.carpooling.common.mapper.OrderMapper;
import com.carpooling.common.pojo.db.*;
import com.carpooling.common.pojo.vo.*;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.service.*;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 预估性能瓶颈：orderList的查询或许会出现慢sql，可以在latestTime上加索引。
 *
 * @author LiangHanSggg
 * @date 2023-08-03 14:15
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    OrderMapper orderMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    CommonService commonService;

    @Autowired
    BatchService batchService;

    @Autowired
    OrderBatchService orderBatchService;

    @Autowired
    OrderUserService orderUserService;

    @Autowired
    BlackListService blackListService;

    @Autowired
    UserService userService;

    @Autowired
    ApplicationContext applicationContext;


    static DateTimeFormatter time_formatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public Map<String, Object> orderList(int index, int size) {
        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery(Order.class)
                .gt(Order::getLatestTime, LocalDateTime.now())
                .eq(Order::getState, 0)
                .orderByAsc(Order::getLatestTime);

        Page<Order> p = new Page<>(index, size);

        page(p, wrapper);
        List<Order> records = p.getRecords();
        if (records.isEmpty()) return null;


        Long userId = UserContext.get().getId();
        Map<String, Object> res = new HashMap<>();
        res.put("records", getOrderInfoVO(records, userId));
        res.put("total", p.getTotal());
        return res;
    }

    @Override
    public Map<String, Object> conditionOrder(OderListConditionVO condition) {
        condition.setIndex((condition.getIndex().intValue() - 1) * condition.getPage());
        List<Order> orders = orderMapper.conditionList(condition);
        if (orders == null || orders.isEmpty()) return null;
        Map<String, Object> res = new HashMap<>();
        res.put("records", getOrderInfoVO(orders, UserContext.get().getId()));
        res.put("total", orders.size());
        return res;
    }

    @Override
    public String getToken() {
        String token = IdUtil.simpleUUID();
        redisUtil.StringAdd(RedisPrefix.ORDER_ACCESS + UserContext.get().getId(), token, 15, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public String createOrder(PlaceOrderVO placeOrderVO) {

        Long userId = UserContext.get().getId();

        Integer qualification = commonService.checkAndQualificationCancel(userId, RedisPrefix.ORDER_LIMIT + userId, "24小时内只能创建三个订单（不与拼单合计）", placeOrderVO.getAccessToken());


        User uInfo = userService.getOne(Wrappers.lambdaQuery(User.class)
                .eq(User::getId, userId)
                .select(User::getPhone, User::getAccount, User::getName, User::getReliabilityRating));

        if (uInfo.getReliabilityRating() < 80) throw new OrderVerifyException("信誉分过低");


        LambdaQueryWrapper<Order> eq = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOwnerId, userId)
                .eq(Order::getState, 0);

        long count = count(eq);
        if (count != 0) throw new OrderVerifyException("您有存在的订单未结束");

        // 检查自己是否有批次和这个订单发生冲突
        LambdaQueryWrapper<Batch> eq1 = Wrappers.lambdaQuery(Batch.class)
                .eq(Batch::getUserId, userId)
                .eq(Batch::getStartPlace, 0);

        // 一个用户24小时只能发起3次拼车， 一个单子最多能下到4天后。因此一个用户4天内最多12行处于未结束记录。
        List<Batch> list = batchService.list(eq1);

        boolean flag = false;


        for (int i = 0; i < list.size(); i++) {
            Batch batch = list.get(i);
            if (batch.getStartPlace().equals(placeOrderVO.getStartPlace())
                    && batch.getEndPlace().equals(placeOrderVO.getEndPlace())
                    && LocalDateTimeUtil.isSameDay(placeOrderVO.getLatestTime(), batch.getLatestTime())) {
                flag = true;
                break;
            }
        }


        Order order = new Order();
        BeanUtil.copyProperties(placeOrderVO, order, "accessToken");
        order.setCreatePersonId(userId);
        order.setOwnerId(userId);
        order.setOwnerOpenid(UserContext.get().getOpenid());
        order.setAppointmentTime(placeOrderVO.getLatestTime().toLocalDate());
        order.setId(IdWorker.getId());

        OrderUser orderUser = new OrderUser();
        orderUser.setOrderId(order.getId())
                .setUserId(userId)
                .setUserName(uInfo.getName())
                .setUserSex(order.getSex())
                .setPersonNumber(placeOrderVO.getAlreadyNumber())
                .setUserPhone(uInfo.getPhone())
                .setUserWechatAccount(uInfo.getAccount())
                .setUserOpenid(order.getOwnerOpenid())
                .setUserRole(0)
                .setStartPlace(order.getStartPlace())
                .setEndPlace(order.getEndPlace())
                .setAppointmentTime(order.getAppointmentTime());


        if (!applicationContext.getBean(this.getClass()).orderAndUser(order, orderUser)) {
            log.error("订单保存失败，订单详情:[{}]", order);
            throw new OrderVerifyException("订单创建失败，稍后重试或联系管理员");
        }

        commonService.afterPlaceOrder(qualification, RedisPrefix.ORDER_LIMIT + userId, 200, 24);

        return flag ? "创建拼单成功，存在可能冲突的批次" : "创建拼单成功";
    }

    @Override
    public String authJoin(AutoJoinVO autoJoinVO) {

        Long userId = UserContext.get().getId();

        Integer qualification = commonService.checkAndQualificationCancel(userId, RedisPrefix.ORDER_AUTO_JOIN_LIMIT + userId, "24小时内自动加入次数限制6次", autoJoinVO.getAccessToken());

        if (!userService.checkReliability(userId)) {
            throw new OrderVerifyException("信誉分过低");
        }

        // 到进入CAS之前，这俩个方法可以使用异步任务编排加快速度
        Order targetOrder = getOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getId, autoJoinVO.getOrderId()));

        if (Objects.isNull(targetOrder)) {
            // 选择了不存在的订单。 在订单不可删除的情况下这个操作是违法的 直接拉黑
            BlackList blackList = new BlackList();
            blackList.setUserId(userId);
            blackListService.save(blackList);
            log.error("绕开前端验证,下单不存在的订单,userId:{},userIP:{}", userId, UserContext.get().getClientIP());
            throw new OrderVerifyException("已被拉黑，联系管理员");
        }

        if (targetOrder.getAutoJoin().intValue() == 0) {
            throw new RuntimeException("非自动加入不应该请求这个接口");
        }

//        if (targetOrder.getLatestTime().isBefore(LocalDateTime.now())) {
//            throw new OrderVerifyException("已经超过最晚发车时间");
//        }
//        if (LocalDateTimeUtil.between(targetOrder.getLatestTime(), LocalDateTime.now()).toMinutes() <= 3) {
//            throw new OrderVerifyException("距离最晚时间过短不得加入");
//        }


        {
            int i1 = targetOrder.getSex().intValue();
            if (i1 != 0 && autoJoinVO.getSex().intValue() != i1) {
                throw new OrderVerifyException("不符合本订单的性别限制");
            }
        }

        LambdaQueryWrapper<OrderUser> select = Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getUserId, userId)
                .eq(OrderUser::getState, 0)
                .select(OrderUser::getOrderId, OrderUser::getStartPlace, OrderUser::getEndPlace, OrderUser::getAppointmentTime);

        // 有可能自己加自己的订单，有可能自己就在这个订单中
        List<OrderUser> list = orderUserService.list(select);

        boolean flag = false;
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                OrderUser orderUser = list.get(i);
                if (orderUser.getOrderId().equals(targetOrder.getId())) {
                    // 订单冲突，
                    if (orderUser.getState().intValue() == 1) {
                        throw new OrderVerifyException("已加入本车次，请勿重复加入");
                    } else if (orderUser.getState().intValue() == 0) {
                        BlackList blackList = new BlackList();
                        blackList.setUserId(userId);
                        blackListService.save(blackList);
                        log.error("绕开前端验证,加入自己的订单,userId:{},userIP:{}", userId, UserContext.get().getClientIP());
                        throw new OrderVerifyException("已被拉黑，联系管理员");
                    }
                }

                if (LocalDateTimeUtil.isSameDay(orderUser.getAppointmentTime(), targetOrder.getAppointmentTime())
                        && orderUser.getStartPlace().equals(targetOrder.getStartPlace())
                        && orderUser.getEndPlace().equals(targetOrder.getEndPlace())) {
                    flag = true;
                    break;
                }
            }
        }

        int count = 0;

        while (count++ < 3 && dbCAS(autoJoinVO)) {

            User uInfo = userService.getOne(Wrappers.lambdaQuery(User.class)
                    .eq(User::getId, userId)
                    .select(User::getPhone, User::getAccount, User::getName));

            OrderUser orderUser = new OrderUser()
                    .setOrderId(targetOrder.getId())
                    .setUserId(userId)
                    .setUserName(uInfo.getName())
                    .setUserSex(autoJoinVO.getSex())
                    .setPersonNumber(autoJoinVO.getPersonNumber())
                    .setUserPhone(uInfo.getPhone())
                    .setUserWechatAccount(uInfo.getAccount())
                    .setUserOpenid(UserContext.get().getOpenid())
                    .setUserRole(1)
                    .setStartPlace(targetOrder.getStartPlace())
                    .setEndPlace(targetOrder.getEndPlace())
                    .setAppointmentTime(targetOrder.getAppointmentTime());

            if (!orderUserService.save(orderUser)) {
                // 这里应该做事务回滚的操作。将人数增加回去，虽然会失败的概率很低。
                log.error("自动加入失败，历史订单[{}]", orderUser);
                throw new RuntimeException();
            }

            commonService.afterPlaceOrder(qualification, RedisPrefix.ORDER_AUTO_JOIN_LIMIT + userId, 5, 24);

            return flag ? "下单成功，存在有可能与行程发生冲突的订单，请核实" : "下单成功";
        }

        throw new OrderVerifyException("下单失败");

    }

    @Override
    public List<OrderInfoVO> getRecommended(RecommendVO recommendVO) {
        Long userId = UserContext.get().getId();


        User one = userService.getOne(Wrappers.lambdaQuery(User.class)
                .eq(User::getId, userId)
                .select(User::getWaitTime));

        if (Objects.isNull(one)) throw new RuntimeException();

        Integer waitTime = one.getWaitTime();

        // 大数据量的时候先获得当天的全部id，然后


        // 先获得时间。 最早出发-配置的时间 >= 0.
        // 最晚出发+配置时间<=12.
        LocalDateTime start = null;

        LocalDateTime offset = LocalDateTimeUtil.offset(recommendVO.getStartTime(), -waitTime, ChronoUnit.MINUTES);
        LocalDateTime beginOfDay = LocalDateTimeUtil.beginOfDay(recommendVO.getStartTime());
        if (beginOfDay.isBefore(offset)) {
            start = offset;
        } else {
            start = beginOfDay;
        }

        LocalDateTime endTime = null;
        LocalDateTime offset1 = LocalDateTimeUtil.offset(recommendVO.getLastTime(), waitTime, ChronoUnit.MINUTES);
        LocalDateTime endOfDay = LocalDateTimeUtil.endOfDay(recommendVO.getLastTime());
        if (endOfDay.isBefore(offset1)) {
            endTime = endOfDay;
        } else {
            endTime = offset1;
        }

        /**
         * 数据量大的时候可以使用这个sql，并且在appointment_time上加索引。
         * select *
         * from car_order
         *          right join (
         *     select id
         *     from car_order
         *     where appointment_time = ?
         * ) as b on car_order.id = b.id;
         */


        List<Order> list = list(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getStartPlace, recommendVO.getStartPlace())
                .eq(Order::getEndPlace, recommendVO.getEndPlace())
                .eq(Order::getState, 0)
                .ge(Order::getEarliestTime, start)
                .le(Order::getLatestTime, endTime));

        if (list == null || list.isEmpty()) return null;

        return getOrderInfoVO(list, userId);
    }

    @Override
    public List<OrderDetailVO> getOrderDetail() {
        Long userId = UserContext.get().getId();

        LambdaQueryWrapper<Order> eq = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOwnerId, userId);

        List<Order> list = list(eq);
        if (list == null || list.isEmpty()) return null;

//        // 约束是一单。 不应该约束一单，如果出现转让的时候如果单子里的人都有自己的单子，那么会出现异常
//        if (list.size() >= 2) {
//            throw new OrderVerifyException("出现异常，联系管理员");
//        }

        List<OrderDetailVO> res = new ArrayList<>();

        for (Order order : list) {
            // 跳过已经结束或是完成的订单
            if (order.getState().intValue() == 1 || order.getState().intValue() == 3) {
                continue;
            }
            OrderDetailVO orderDetailVO = createOrderDetailVO(order, userId);
            if (orderDetailVO != null) res.add(orderDetailVO);
        }


        return res;
    }

    @Override
    public List<OrderBriefInfoVO> getSelf() {
        Long id = UserContext.get().getId();


        LambdaQueryWrapper<OrderUser> eq = Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getUserId, id)
                .eq(OrderUser::getState, 0)
                .eq(OrderUser::getUserRole, 1)
                .select(OrderUser::getOrderId);

        List<OrderUser> list = orderUserService.list(eq);
        if (list == null || list.isEmpty()) return null;

        Long[] longs = list.stream().map(orderUser -> orderUser.getOrderId()).toArray(Long[]::new);
        // 后期根据前端的需求更改字段
        List<OrderBriefInfoVO> orderBriefInfoVOS = batchService.orderCollect(longs);
        orderBriefInfoVOS.forEach(orderBriefInfoVO -> {
            orderBriefInfoVO.setOwnerId(null);
        });
        return orderBriefInfoVOS;

    }

    @Override
    public OrderDetailVO JoinOrderDetail(Long orderId) {
        Long userId = UserContext.get().getId();

        // 检查现在在订单的状态
        LambdaQueryWrapper<OrderUser> eq = Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getOrderId, orderId)
                .eq(OrderUser::getUserId, userId);

        OrderUser one = orderUserService.getOne(eq);
        if (one == null) {
            log.error("用户{}，查不到OrderUser记录,订单号:{}", userId, orderId);
            throw new OrderVerifyException("订单出现异常，请联系管理员");
        }
        if (one.getUserRole() == 0) {
            throw new OrderVerifyException("请勿在此查询自己创建的订单");
        }


        LambdaQueryWrapper<Order> eq1 = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getId, orderId);

        Order order = getOne(eq1);
        if (order == null) throw new OrderVerifyException("订单不存在");

        // 已经成为历史订单
        if (one.getState().intValue() != 0 || one.getState().intValue() != 2) throw new OrderVerifyException("订单已经结束");


        OrderDetailVO orderDetail = getOrderDetail(order);
        //获得乘客列表
        List<OrderUser> passenger = orderUserService.list(Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getOrderId, order.getId())
                .eq(OrderUser::getState, 0)
                .select(OrderUser::getUserId, OrderUser::getUserSex, OrderUser::getUserName, OrderUser::getUserPhone, OrderUser::getUserWechatAccount, OrderUser::getUserRole));

        if (passenger == null || passenger.isEmpty()) return orderDetail;

        List<PassengerVO> passengerVOS = new ArrayList<>();

        for (OrderUser orderUser : passenger) {
            PassengerVO passengerVO = new PassengerVO();
            boolean flag = false;
            // 除了用户自身和团长，剩下的都脱敏
            if (orderUser.getUserId().equals(userId) || (flag = (orderUser.getUserRole() == 0))) {
                passengerVO.setUserId(orderUser.getUserId())
                        .setUserName(orderUser.getUserName())
                        .setSex(orderUser.getUserSex())
                        .setPhone(orderUser.getUserPhone())
                        .setWxAccount(orderUser.getUserWechatAccount());
                if (flag) {
                    passengerVO.setOwner(1);
                }

            } else {
                passengerVO.setUserName(orderUser.getUserName().substring(0, 1) + "同学")
                        .setPhone(DesensitizedUtil.mobilePhone(orderUser.getUserPhone()))
                        .setSex(orderUser.getUserSex());
            }
            passengerVOS.add(passengerVO);
        }

        orderDetail.setPassengerList(passengerVOS);

        return orderDetail;
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        // 检查自己是否在单子内， 状态是否正常，自己是不是持有者。

        Long id = UserContext.get().getId();

        LambdaQueryWrapper<OrderUser> select = Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getOrderId, orderId)
                .eq(OrderUser::getUserId, id)
                .select(OrderUser::getId, OrderUser::getState, OrderUser::getUserRole, OrderUser::getPersonNumber);


        OrderUser one = orderUserService.getOne(select);

        if (one == null) throw new OrderVerifyException("您不在订单中");
        int state;
        if ((state = one.getState().intValue()) != 0) {
            if (state == 1) throw new OrderVerifyException("订单已结束");
            if (state == 2) throw new OrderVerifyException("您已退出订单");
            if (state == 3) throw new OrderVerifyException("您已不存在于此订单");
        }
        if (one.getUserRole().intValue() == 0) {
            throw new OrderVerifyException("您拥有此订单");
        }


        int count = 0;

        while (count++ < 3 && cancelCAS(orderId, one.getPersonNumber())) {

            // 个人认为batch是不用进行状态修改的，因为已经存在于订单过，那么意味着对应的批次的状态是结束。而不是创建这个状态
            // 进行扣分，并且将orderUser中的状态进行修改
            applicationContext.getBean(this.getClass()).cancelTransactional(one.getId(), id);

            return true;
        }


        return false;
    }

    @Override
    public boolean leaderCancelOrder(Long orderId) {
        Long userId = UserContext.get().getId();
        // 检查单子是不是自己的。 并且查看是否超时了。 没有超时就取消，并且按照加入进来的人的
        LambdaQueryWrapper<Order> eq = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getId, orderId);
        Order one = getOne(eq);

        if (Objects.isNull(one)) throw new OrderVerifyException("不存在订单");

        if (!one.getOwnerId().equals(userId)) throw new OrderVerifyException("您不拥有此订单");


        return cancelOrderTransactional(orderId, userId);
    }

    @Override
    public boolean leaderCompleteOrder(Long orderId) {

        // 首先要分是自动加入还是非自动加入。
        /**
         * 自动加入。
         *  将单状态修改。
         *  修改成员的属性。增加次数。
         *  修改成员对应order-user表的状态。
         *
         * 非自动加入。
         *  将单状态修改。
         *  修改成员的属性。增加次数。
         *  修改成员对应order-user表的状态。
         *  修改直接修改OderBatch
         */

//               将单状态修改。
        Long id = UserContext.get().getId();

        LambdaQueryWrapper<Order> eq = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getId, orderId);

        Order one = getOne(eq);

        if (Objects.isNull(one)) throw new OrderVerifyException("不存在订单");

        if (!id.equals(one.getCreatePersonId())) throw new OrderVerifyException("您不拥有此订单");

        LambdaUpdateWrapper<Order> set = Wrappers.lambdaUpdate(Order.class)
                .eq(Order::getId, orderId)
                .set(Order::getState, 1);

        if (!update(set)) return false;

//        找到订单内的全部成员，修改orderUser的状态
        LambdaQueryWrapper<OrderUser> orderUserLambdaQueryWrapper = Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getOrderId, orderId)
                .eq(OrderUser::getState, 0);

        List<OrderUser> orderUserList = orderUserService.list(orderUserLambdaQueryWrapper);

//        获得这些成员的idlist。
        List<Long> idList = new LinkedList<>();
        orderUserList.forEach(orderUser -> {
            orderUser.setState(1);
            idList.add(orderUser.getUserId());
        });


        if (!orderUserService.updateBatchById(orderUserList)) {
            return false;
        }


//       修改成员的属性。增加次数。
        for (Long uId : idList) {
            LambdaUpdateWrapper<User> userLambdaUpdateWrapper = Wrappers.lambdaUpdate(User.class)
                    .eq(User::getId, uId)
                    .setSql("successes_number = successes_number + 1")
                    .setSql("total_number = total_number +1");
            userService.update(userLambdaUpdateWrapper);
        }

//        如果是自动加入的直接结束订单。
        if (one.getAutoJoin().intValue() == 1) return true;

//        需要修改orderBatch和batch
//        得从ob找batch

        //        每个用户都得执行这样的操作，所以是在for循环里面
        for (Long uId : idList) {
            LambdaQueryWrapper<OrderBatch> eq1 = Wrappers.lambdaQuery(OrderBatch.class)
                    .eq(OrderBatch::getBatchId, orderId)
                    .eq(OrderBatch::getOwnerId, uId);
            OrderBatch one1 = orderBatchService.getOne(eq1);
            Long batchId = one1.getBatchId();
            LambdaQueryWrapper<Batch> eq2 = Wrappers.lambdaQuery(Batch.class)
                    .eq(Batch::getId, batchId);

            Batch one2 = batchService.getOne(eq2);
            one2.setState(1);
            batchService.updateById(one2);
        }

        return true;
    }

    @Override
    public boolean leadRemoveUser(Long orderId, Long userID) {
        // 先判断该订单的类型，是自动加入还是非自动加入
        LambdaQueryWrapper<Order> select = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getId, orderId);

        Order one = getOne(select);
        if (one == null) return false;

//        if (one.getAutoJoin().intValue() == 1) {
        // 自动加入

        // 现在先要获得批次的人数。
        LambdaQueryWrapper<OrderUser> eq = Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getOrderId, orderId)
                .eq(OrderUser::getUserId, userID);
        OrderUser one1 = orderUserService.getOne(eq);
        // 获得批次的人数
        Integer personNumber = one1.getPersonNumber();
//            修改这个orderuser的状态
        one1.setState(3);
        // 修改订单
        one.setAlreadyNumber(one.getAlreadyNumber() - personNumber);

//            同时更新成功才算是成功
        return updateById(one) && orderUserService.updateById(one1);
//        暂时是无需去处理orderBatch的内容。
//        } else {
//            // 非自动加入
//
//            // 现在先要获得批次的人数。
//            LambdaQueryWrapper<OrderUser> eq = Wrappers.lambdaQuery(OrderUser.class)
//                    .eq(OrderUser::getOrderId, orderId)
//                    .eq(OrderUser::getUserId, userID);
//            OrderUser one1 = orderUserService.getOne(eq);
//            // 获得批次的人数
//            Integer personNumber = one1.getPersonNumber();
////            修改这个orderuser的状态
//            one1.setState(3);
//            // 修改订单
//            one.setAlreadyNumber(one.getAlreadyNumber() - personNumber);
//
////            同时更新成功才算是成功
//            boolean b = updateById(one);
//            boolean b1 = orderUserService.updateById(one1);
//            if (!b || !b1) return false;
//
////            先找到对应的order_batch记录
//            LambdaQueryWrapper<OrderBatch> eq1 = Wrappers.lambdaQuery(OrderBatch.class)
//                    .eq(OrderBatch::getOrderId, orderId)
//                    .eq(OrderBatch::getOwnerId, userID);
//
//            OrderBatch one2 = orderBatchService.getOne(eq1);
//            if (one2 == null) return false;
//
//
//
//        }


//        return false;
    }

    /**
     * 创建乘客信息表，不包含团长自己的
     *
     * @param list
     * @param userId
     * @return
     */
    public List<PassengerVO> passengerVOList(List<OrderUser> list, Long userId) {
        List<PassengerVO> res = new ArrayList<>();

        list.forEach(orderUser -> {
            if (orderUser.getUserId().equals(userId)) {
                return;
            }

            PassengerVO passengerVO = new PassengerVO()
                    .setUserId(orderUser.getUserId())
                    .setUserName(orderUser.getUserName())
                    .setSex(orderUser.getUserSex())
                    .setPhone(orderUser.getUserPhone())
                    .setWxAccount(orderUser.getUserWechatAccount());

            res.add(passengerVO);
        });

        return res;
    }


    /**
     * 在首页展示的时候不会展示属于自己的正在拼单的单子。
     * <p>
     * 载入到进程之后判断？还是增加mysql条件判断？
     * 个人认为在sql语句中增加判断会变慢。本身首页查询极有可能是瓶颈点，再加上一个<>个人感觉得不偿失
     * 而且理论上一个人只存在一单。不如载入到进程进行循环的时候判断就行了。
     *
     * @param list
     * @param userId
     * @return
     */
    public List<OrderInfoVO> getOrderInfoVO(List<Order> list, Long userId) {
        List<OrderInfoVO> res = new ArrayList<>();
        list.forEach(order -> {

            OrderInfoVO orderInfoVO = new OrderInfoVO()
                    .setOrderId(order.getId())
                    .setStartPlace(order.getStartPlace())
                    .setEndPlace(order.getEndPlace())
                    .setAppointmentTime(order.getAppointmentTime().toString())
                    .setEarliestTime(time_formatter.format(order.getEarliestTime()))
                    .setLatestTime(time_formatter.format(order.getLatestTime()))
                    .setAlreadyNumber(order.getAlreadyNumber())
                    .setTargetNumber(order.getTargetNumber())
                    .setSex(order.getSex())
                    .setAutoJoin(order.getAutoJoin());

            if (userId.equals(order.getOwnerId())) orderInfoVO.setMyself(1);
            else orderInfoVO.setMyself(0);

            res.add(orderInfoVO);
        });
        return res;
    }

    /**
     * 创建OrderDetailVO 只创建VO中的订单部分，不包含乘客
     *
     * @param order
     * @return
     */
    public OrderDetailVO getOrderDetail(Order order) {

        OrderDetailVO orderDetailVO = new OrderDetailVO();
        orderDetailVO.setOrderId(order.getId())
                .setStartPlace(order.getStartPlace())
                .setEndPlace(order.getEndPlace())
                .setAppointmentTime(order.getAppointmentTime().toString())
                .setEarliestTime(time_formatter.format(order.getEarliestTime()))
                .setLatestTime(time_formatter.format(order.getLatestTime()))
                .setAlreadyNumber(order.getAlreadyNumber())
                .setTargetNumber(order.getTargetNumber())
                .setSex(order.getSex())
                .setAutoJoin(order.getAutoJoin());
        orderDetailVO.setState(order.getState());

        return orderDetailVO;

    }

    /**
     * 创建OrderDetailVO 建乘客部分,并且对信息进行修改
     * <p>
     * 尝试抽离...但是前期没考虑，现在有点麻烦。第二版的时候可以把乘客信息的转换抽出来(passengerVOList)，前面是一样的，区别在于乘客信息如何进行针对性的脱敏
     *
     * @param order
     * @param userId
     * @return
     */
    public OrderDetailVO createOrderDetailVO(Order order, Long userId) {

        OrderDetailVO orderDetail = getOrderDetail(order);
        // 获得乘客列表 现在在订单里面的乘客
        List<OrderUser> passenger = orderUserService.list(Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getOrderId, order.getId())
                .eq(OrderUser::getState, 0)
                .select(OrderUser::getUserId, OrderUser::getUserSex, OrderUser::getUserName, OrderUser::getUserPhone, OrderUser::getUserWechatAccount));

        if (passenger == null || passenger.isEmpty()) return orderDetail;

        List<PassengerVO> passengerVOS = passengerVOList(passenger, userId);

        orderDetail.setPassengerList(passengerVOS);
        return orderDetail;
    }

    /**
     * 自动加入用到的CAS
     *
     * @param autoJoinVO
     * @return
     */
    public boolean dbCAS(AutoJoinVO autoJoinVO) {
        Order targetOrder = getOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getId, autoJoinVO.getOrderId())
                .select(Order::getId, Order::getAlreadyNumber, Order::getTargetNumber, Order::getVersion, Order::getState));
        if (targetOrder.getState().intValue() != 0) throw new OrderVerifyException("订单已经结束");
        int nu = autoJoinVO.getPersonNumber() + targetOrder.getAlreadyNumber();
        if (targetOrder.getTargetNumber() < nu)
            throw new OrderVerifyException("订单人数不符合要求");
        targetOrder.setAlreadyNumber(nu);
        return updateById(targetOrder);
    }

    /**
     * 减少订单中就绪的人数
     *
     * @param orderId 订单号
     * @param number  要减少的人数
     * @return
     */
    public boolean cancelCAS(Long orderId, int number) {

        Order order = getOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getId, orderId)
                .select(Order::getId, Order::getAlreadyNumber, Order::getVersion, Order::getState));
        if (order.getState().intValue() != 0) throw new OrderVerifyException("订单已经结束");
        int nu = order.getAlreadyNumber() - number;
        if (nu <= 0) throw new RuntimeException();
        order.setAlreadyNumber(nu);
        return updateById(order);
    }


    // 对个人分数进行扣减，对`用户记录`进行状态修改
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean cancelTransactional(Long orderUserId, Long userId) {

        boolean userUpdate = userService.update(Wrappers.lambdaUpdate(User.class)
                .eq(User::getId, userId)
                .setSql("reliability_rating = reliability_rating - 5"));


        boolean logUpdate = orderUserService.update(Wrappers.lambdaUpdate(OrderUser.class)
                .eq(OrderUser::getId, orderUserId)
                .set(OrderUser::getState, 2));

        if (userUpdate && logUpdate) return true;

        log.error("cancelTransactional失败，orderUserId:[{}],userId:[{}]", orderUserId, userId);
        throw new RuntimeException();
    }

    // 对个人分数进行扣减，对`订单`进行状态修改
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean cancelOrderTransactional(Long orderId, Long userId) {
        LambdaUpdateWrapper<Order> set = Wrappers.lambdaUpdate(Order.class)
                .eq(Order::getId, orderId)
                .set(Order::getState, 3);

        boolean updateOrder = update(set);

        LambdaUpdateWrapper<User> userLambdaUpdateWrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getId, userId)
                .setSql("reliability_rating = reliability_rating - 5");

        boolean updateUser = userService.update(userLambdaUpdateWrapper);

        if (updateUser && updateOrder) return true;

        log.error("cancelTransactional失败，orderId:[{}],userId:[{}]", orderId, userId);
        throw new RuntimeException();
    }


    @Transactional(rollbackFor = RuntimeException.class)
    public boolean orderAndUser(Order order, OrderUser orderUser) {
        if (save(order) && orderUserService.save(orderUser)) {
            return true;
        } else {
            log.error("orderAndUser失败，order:[{}],orderUser:[{}]", order, orderUser);
            throw new RuntimeException();
        }
    }

}
