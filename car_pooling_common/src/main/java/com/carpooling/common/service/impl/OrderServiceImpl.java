package com.carpooling.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    OrderUserService orderUserService;

    @Autowired
    BlackListService blackListService;

    @Autowired
    UserService userService;

    @Autowired
    ApplicationContext applicationContext;


    static DateTimeFormatter time_formatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public List<OrderInfoVO> orderList(int index, int size) {
        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery(Order.class)
                .gt(Order::getLatestTime, LocalDateTime.now())
                .eq(Order::getState, 0)
                .orderByAsc(Order::getLatestTime);

        Page<Order> p = new Page<>(index, size);

        page(p, wrapper);
        List<Order> records = p.getRecords();
        if (records.isEmpty()) return null;


        Long userId = UserContext.get().getId();
        return getOrderInfoVO(records, userId);
    }

    @Override
    public List<OrderInfoVO> conditionOrder(OderListConditionVO condition) {
        condition.setIndex((condition.getIndex().intValue() - 1) * condition.getPage());
        List<Order> orders = orderMapper.conditionList(condition);
        if (orders == null || orders.isEmpty()) return null;
        return getOrderInfoVO(orders, UserContext.get().getId());
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

        commonService.afterPlaceOrder(qualification, RedisPrefix.ORDER_LIMIT + userId, 2, 24);

        return flag ? "创建拼单成功，存在可能冲突的批次" : "创建拼单成功";
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public boolean orderAndUser(Order order, OrderUser orderUser) {
        return save(order) && orderUserService.save(orderUser);
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
                if (orderUser.getOrderId() == targetOrder.getId()) {
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

    public boolean dbCAS(AutoJoinVO autoJoinVO) {
        Order targetOrder = getOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getId, autoJoinVO.getOrderId())
                .select(Order::getId, Order::getAlreadyNumber, Order::getTargetNumber, Order::getVersion));

        int nu = autoJoinVO.getPersonNumber() + targetOrder.getAlreadyNumber();
        if (targetOrder.getTargetNumber() < nu)
            throw new OrderVerifyException("订单人数不符合要求");
        targetOrder.setAlreadyNumber(nu);
        return updateById(targetOrder);
    }

}
