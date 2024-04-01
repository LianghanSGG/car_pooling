package com.carpooling.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.exception.OrderVerifyException;
import com.carpooling.common.mapper.BatchMapper;
import com.carpooling.common.pojo.BaseEntity;
import com.carpooling.common.pojo.BatchDTO;
import com.carpooling.common.pojo.db.*;
import com.carpooling.common.pojo.vo.BatchVO;
import com.carpooling.common.pojo.vo.OrderBriefInfoVO;
import com.carpooling.common.pojo.vo.ShoppingCarVo;
import com.carpooling.common.pojo.vo.UserSimpleInfoVO;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.service.*;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author LiangHanSggg
 * @date 2023-08-02 20:47
 */
@Slf4j
@Service
public class BatchServiceImpl extends ServiceImpl<BatchMapper, Batch> implements BatchService {


    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderService orderService;

    @Autowired
    BlackListService blackListService;

    @Autowired
    UserService userService;

    @Autowired
    OrderBatchService orderBatchService;

    @Autowired
    OrderUserService orderUserService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CommonService commonService;

    @Autowired
    BatchService batchService;


    @Override
    public List<BatchVO> getBatchList() {
        Long id = UserContext.get().getId();
        LambdaQueryWrapper<Batch> batchLambdaQueryWrapper = Wrappers.lambdaQuery(Batch.class)
                .eq(Batch::getState, 0)
                .eq(Batch::getUserId, id)
                .lt(Batch::getLatestTime, LocalDateTime.now())
                .orderByAsc(BaseEntity::getCreateTime);
        List<Batch> list = list(batchLambdaQueryWrapper);
        if (list.isEmpty()) return null;
        List<BatchVO> res = new ArrayList<>();
        list.forEach(batch -> {
            BatchVO batchVO = new BatchVO();
            batchVO.setBatchId(batch.getId());
            batchVO.setStartPlace(batch.getStartPlace());
            batchVO.setEndPlace(batch.getEndPlace());
            res.add(batchVO);
        });

        return res;
    }


    /**
     * 可以使用模板方法、门面模，实现`下单`。但是我们这里下单只有俩个方法实现，第二版可以考虑要不要加设计模式。
     * 下单分三步， 前期资质核销、中期下单服务、后期状态更新。
     */

    @Override
    public String createBatch(ShoppingCarVo shoppingCarVo) {

        Long userId = UserContext.get().getId();

        // 不通过直接会抛异常结束
        Integer qualification = commonService.checkAndQualificationCancel(userId, RedisPrefix.BATCH_LIMIT + userId, "24小时内限制3次申请（不与创建订单合算）", shoppingCarVo.getAccessToken());


        LambdaQueryWrapper<User> select2 = Wrappers.lambdaQuery(User.class)
                .eq(User::getId, userId)
                .select(User::getNickName, User::getReliabilityRating);

        User user = userService.getOne(select2);

        if (user.getReliabilityRating() < 80) throw new OrderVerifyException("信誉分过低");


        /**
         *  ....懒得去改了方法名了，上面实际是正式提交订单前的`防重复`校验......
         */
        Long[] orderList = Arrays.stream(shoppingCarVo.getOrderList()).distinct().toArray(Long[]::new);

        // 减少JVM的局部变量表的大小
        {
            int tempLength = shoppingCarVo.getOrderList().length;

            if (tempLength != orderList.length) {
                BlackList blackList = new BlackList();
                blackList.setUserId(userId);
                blackListService.save(blackList);
                log.error("绕开前端验证,自己下单自己的车次,userId:{},userIP:{}", userId, UserContext.get().getClientIP());
                throw new OrderVerifyException("已被拉黑，联系管理员");
            }
        }


        // 查出来的订单有可能已经结束状态了
        List<OrderBriefInfoVO> orderBriefInfoVOS = orderCollect(orderList);

        if (orderBriefInfoVOS == null || orderBriefInfoVOS.isEmpty()) throw new OrderVerifyException("列表中的订单都结束");

        BatchDTO batchDTO = verifyOrderInfo(shoppingCarVo, orderBriefInfoVOS, userId, user);

        Batch batch = batchDTO.getBatch();
        Long id = batch.getId();
        batchDTO.getOrderBatchList().forEach(e -> e.setBatchId(id));


        if (!applicationContext.getBean(this.getClass()).insertOrderIntoDB(batchDTO)) {
            log.error("创建批次和订单批次失败，BatchDTO:[{}]", batchDTO);
            throw new OrderVerifyException("创建失败，稍后重试或联系管理员");
        }


        commonService.afterPlaceOrder(qualification, RedisPrefix.BATCH_LIMIT + userId, 2, 24);

        return "创建成功";

    }


    /**
     * 将Batch和OrderBatch存入到数据库。事务.
     *
     * @param batchDTO
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean insertOrderIntoDB(BatchDTO batchDTO) {
        return save(batchDTO.getBatch()) && orderBatchService.saveBatch(batchDTO.getOrderBatchList());
    }


    @Override
    public List<OrderBriefInfoVO> orderCollect(Long[] orderList) {
        List<OrderBriefInfoVO> list = new ArrayList<>(5);
        List<Long> temp = new ArrayList<>(5);

        for (Long orderId : orderList) {
            OrderBriefInfoVO orderBriefInfoVO = (OrderBriefInfoVO) redisUtil.StringGet(RedisPrefix.ORDER_BRIEF_INFO + orderId, OrderBriefInfoVO.class);
            if (Objects.isNull(orderBriefInfoVO)) {
                temp.add(orderId);
            } else {
                list.add(orderBriefInfoVO);
            }
        }
        if (temp.isEmpty()) return list;

        // from `order` where id in (id1,id2,id3);
        LambdaQueryWrapper<Order> in = Wrappers.lambdaQuery(Order.class)
                .in(Order::getId, temp);

        List<Order> dbOrderList = orderService.list(in);
        // redis中查不到的订单list。去mysql中也查不到。有问题。 前提是订单不可以删除
        if (temp.size() != dbOrderList.size()) {
            log.error("订单列表中存在未知订单号,userId:{},userIP:{}", UserContext.get().getId(), UserContext.get().getClientIP());
            throw new OrderVerifyException("订单列表中有误，已对操作进行记录");
        }

        // 开始进行转换。
        dbOrderList.forEach(order -> {
            if (order.getState().intValue() != 0) return;

            OrderBriefInfoVO orderBriefInfoVO = new OrderBriefInfoVO();
            BeanUtil.copyProperties(order, orderBriefInfoVO);
            redisUtil.AsyncStringADD(RedisPrefix.ORDER_BRIEF_INFO + orderBriefInfoVO.getId(), orderBriefInfoVO, 24 + RandomUtil.randomInt(0, 12), TimeUnit.HOURS);
            list.add(orderBriefInfoVO);
        });

        return list;
    }

    @Override
    public List<UserSimpleInfoVO> listRequest() {
        /**
         * --思路，先获得自己拥有的订单。如果没有直接返回空
         * 							如果有的话，拿着这个订单号去orderBatch_搜，状态味0的。然后拿到用户的id，去搜基础资料。实现回传基础资料。
         */
        Long userId = UserContext.get().getId();

        LambdaQueryWrapper<OrderUser> eq = Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getUserRole, 0)
                .eq(OrderUser::getState, 0)
                .eq(OrderUser::getUserId, userId);

        List<OrderUser> orderList = orderUserService.list(eq);
        if (orderList == null || orderList.size() == 0) return null;

        List<UserSimpleInfoVO> res = new LinkedList<>();
        // 拿到订单id去遍历去搜 orderBatch
        for (OrderUser orderUser : orderList) {
            Long orderId = orderUser.getOrderId();

            LambdaQueryWrapper<OrderBatch> eq1 = Wrappers.lambdaQuery(OrderBatch.class)
                    .eq(OrderBatch::getOrderId, orderId)
                    .eq(OrderBatch::getState, 0)
                    .ne(OrderBatch::getOwnerId, userId);

            List<OrderBatch> userIdList = orderBatchService.list(eq1);

            for (OrderBatch wantJoinUser : userIdList) {
                Long ownerId = wantJoinUser.getOwnerId();

                LambdaQueryWrapper<User> eq2 = Wrappers.lambdaQuery(User.class).eq(User::getId, ownerId);

                User user = userService.getOne(eq2);

                UserSimpleInfoVO userSimpleInfoVO = new UserSimpleInfoVO();

                String name = user.getName().substring(0, 1) + "同学";
                String schoolId = user.getSchoolId().substring(0, 4) + "********";

                userSimpleInfoVO.setUserName(name);
                userSimpleInfoVO.setSchoolId(schoolId);
                userSimpleInfoVO.setOrderBatchId(String.valueOf(wantJoinUser.getId()));

                res.add(userSimpleInfoVO);
            }
        }


        return res;
    }

    @Override
    public boolean passReq(Long orderBatchId) {
//        order-batch修改。order_user新加一条记录。order进行修改。

        LambdaQueryWrapper<OrderBatch> eq = Wrappers.lambdaQuery(OrderBatch.class)
                .eq(OrderBatch::getId, orderBatchId);

        OrderBatch orderBatch = orderBatchService.getOne(eq);
        if (orderBatch == null) {
            return false;
        }

        orderBatch.setState(1);

        if (!orderBatchService.updateById(orderBatch)) {
            return false;
        }

        LambdaQueryWrapper<User> eq1 = Wrappers.lambdaQuery(User.class)
                .eq(User::getId, orderBatch.getOwnerId());

        User user = userService.getOne(eq1);

        LambdaQueryWrapper<Batch> eq2 = Wrappers.lambdaQuery(Batch.class)
                .eq(Batch::getId, orderBatch.getBatchId());

        Batch batch = batchService.getOne(eq2);

        OrderUser orderUser = new OrderUser()
                .setOrderId(orderBatch.getOrderId())
                .setUserId(orderBatch.getOwnerId())
                .setUserName(user.getName())
                .setUserSex(batch.getSex())
                .setPersonNumber(batch.getPersonNumber())
                .setUserPhone(user.getPhone())
                .setUserWechatAccount(user.getAccount())
                .setUserOpenid(user.getOpenid())
                .setUserRole(1)
                .setStartPlace(batch.getStartPlace())
                .setEndPlace(batch.getEndPlace())
                .setAppointmentTime(batch.getLatestTime().toLocalDate());

        String sql = "already_number = already_number + " + batch.getPersonNumber();
        LambdaUpdateWrapper<Order> orderLambdaUpdateWrapper = Wrappers.lambdaUpdate(Order.class)
                .eq(Order::getId, orderBatch.getOrderId())
                .setSql(sql);

        return orderUserService.save(orderUser) && orderService.update(orderLambdaUpdateWrapper);
    }

    @Override
    public boolean refuseReq(Long orderBatchId) {
//        order-batch修改。顺带检查该批次的全部是否被拒绝了。
        LambdaQueryWrapper<OrderBatch> eq = Wrappers.lambdaQuery(OrderBatch.class)
                .eq(OrderBatch::getId, orderBatchId);

        OrderBatch orderBatch = orderBatchService.getOne(eq);
        if (orderBatch == null) {
            return false;
        }

        orderBatch.setState(2);

        if (!orderBatchService.updateById(orderBatch)) {
            return false;
        }
//        算了。。。检查了也没啥意思。反正不展示。


        return true;
    }


    /**
     * 需要校验的东西
     * 1：出发地和目的地是都都一致？ // 一次要干多件事情  // 前端去做提醒就行了。
     * <p>
     * <p>
     * 时间是否有问题？是否出现覆盖？
     * 第一类：申请的订单中时间是否有问题？
     * 第二类： 自己创建的订单                          // `自己创建的批次是否问题` 这个不管，等别人去制裁。？
     * <p>
     * 4. 订单中是否包含有自动加入的？ （绕过前台实现）
     * 5. 前端还需校验选择的车次是不是自己的。
     * <p>
     * <p>
     * 需要判断的东西 ： 时间 、 和自己的单子、 下过已经下过的单子
     *
     *
     * <p>
     * 来到这里如果校验失败的话可以直接拉黑，因为是绕过前端来的。
     * 除了和自己单子校验之外
     * <p>
     * 假设现在是18.00 单子里面`最早`的`最晚发车时间`是18.03发车 告诉他不能提交！要选择距离发车时间>=3分钟的车次
     * 可以在进行单选的时候就对车次进行筛选，而不是在提交订单那一刻再对一个单子里面的每一个车次进行校验
     */
    // 这个暂时是做批量的。 他选了得等申请的，但是只有1个。
    public BatchDTO verifyOrderInfo(ShoppingCarVo shoppingCarVo, List<OrderBriefInfoVO> orderInfoList, Long userId, User user) {

        // 自动加入 ---（选择批次||自己创建）

        // 检查自己是否有订单：
        LambdaQueryWrapper<Order> select = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOwnerId, userId)
                .eq(Order::getState, 0)
                .select(Order::getEarliestTime, Order::getLatestTime);

        Order one = orderService.getOne(select);

        // 最外围的判断是这个单子是不是自己下过了。
        // 需要判断的东西 ： 时间(跳过) 、 和自己的单子(结束)、 下过已经下过的单子（跳过）
        // 跳过----最后判断还剩下多少。


        // id升序排序
        Collections.sort(orderInfoList, Comparator.comparing(OrderBriefInfoVO::getId));

        Long[] longs = orderInfoList.stream().map(i -> i.getId()).toArray(Long[]::new);

        // 如果数组内容比较多的话是会出现索引失效的，应该拆分数组的数量然后俩次IN。
        // select id from OrderBatch where id  in (id1,id2,id3) and userid = userid;
        LambdaQueryWrapper<OrderBatch> select1 = Wrappers.lambdaQuery(OrderBatch.class)
                .eq(OrderBatch::getOwnerId, userId)
                .in(OrderBatch::getOrderId, longs)
                .select(OrderBatch::getOrderId);

        List<OrderBatch> list = orderBatchService.list(select1);

        if (list != null && !list.isEmpty()) {
            longs = list.stream().map(i -> i.getOrderId()).toArray(Long[]::new);
        } else {
            longs = null;
        }

        if (longs != null && longs.length == orderInfoList.size()) throw new OrderVerifyException("订单中的全部车次均已申请过");

        List<OrderBatch> res = new ArrayList<>();
        boolean flag = false;
        int index = 0;

        String startPlace = null;
        String endPlace = null;
        LocalDateTime latestTime = null;
        /**
         * 标识位如果是false意味着没有初始化要进行初始化，
         *      初始化的条件：这个订单不是自己的单子
         */
        for (int i = 0; i < orderInfoList.size(); i++) {
            OrderBriefInfoVO orderBriefInfoVO = orderInfoList.get(i);

            if (longs != null && index < longs.length && orderBriefInfoVO.getId().equals(longs[index])) {
                // 这个单子自己下过了
                index++;
                continue;
            }

            if (!flag) {
                flag = true;
                startPlace = orderBriefInfoVO.getStartPlace();
                endPlace = orderBriefInfoVO.getEndPlace();
                latestTime = orderBriefInfoVO.getLatestTime();
            } else {

                if (!orderBriefInfoVO.getStartPlace().equals(startPlace) || !orderBriefInfoVO.getEndPlace().equals(endPlace)) {
                    BlackList blackList = new BlackList();
                    blackList.setUserId(userId);
                    blackListService.save(blackList);
                    log.error("绕开前端验证,一个批次俩个地方,userId:{},userIP:{}", userId, UserContext.get().getClientIP());
                    throw new OrderVerifyException("已被拉黑，联系管理员");
                }

                if (!LocalDateTimeUtil.isSameDay(latestTime, orderBriefInfoVO.getLatestTime())) {
                    BlackList blackList = new BlackList();
                    blackList.setUserId(userId);
                    blackListService.save(blackList);
                    log.error("绕开前端验证,一个批次不同天,userId:{},userIP:{}", userId, UserContext.get().getClientIP());
                    throw new OrderVerifyException("已被拉黑，联系管理员");
                } else {
                    //更新最晚的时间
                    latestTime = orderBriefInfoVO.getLatestTime().isAfter(latestTime) ? orderBriefInfoVO.getLatestTime() : latestTime;
                }
            }

            if (orderBriefInfoVO.getLatestTime().isBefore(LocalDateTime.now()) || LocalDateTimeUtil.between(LocalDateTime.now(), orderBriefInfoVO.getLatestTime()).toMinutes() <= 1) {
                // 车次已经结束|| 现在距离车次结束不足1分钟 （ 前端的限制是3分钟，有可能老哥在下单那个页面发呆了很久）
                continue;
            }


            if (one != null
                    && LocalDateTimeUtil.isSameDay(orderBriefInfoVO.getLatestTime(), one.getLatestTime())
                    && !(orderBriefInfoVO.getEarliestTime().isAfter(one.getLatestTime()) || orderBriefInfoVO.getLatestTime().isBefore(one.getEarliestTime()))) {
                // 存在订单，并且同一天，并且时间发生重叠
                throw new OrderVerifyException("您有一个与出发时间冲突的订单");

            }

            if (userId.equals(orderBriefInfoVO.getOwnerId())) {
                BlackList blackList = new BlackList();
                blackList.setUserId(userId);
                blackListService.save(blackList);
                log.error("绕开前端验证,自己下单自己的车次,userId:{},userIP:{}", userId, UserContext.get().getClientIP());
                throw new OrderVerifyException("已被拉黑，联系管理员");
            }

            if (orderBriefInfoVO.getAutoJoin().intValue() == 1) {
                BlackList blackList = new BlackList();
                blackList.setUserId(userId);
                blackListService.save(blackList);
                log.error("绕开前端验证,提交自动加入的订单,userId:{},userIP:{}", userId, UserContext.get().getClientIP());
                throw new OrderVerifyException("已被拉黑，联系管理员");
            }

            // 来到这里是正常的
            OrderBatch orderBatch = new OrderBatch();
            // 不设置批次id 因为还没拿到
            orderBatch.setOrderId(orderBriefInfoVO.getId());
            orderBatch.setOwnerId(userId);
            res.add(orderBatch);
        }

        if (res.isEmpty()) throw new OrderVerifyException("请勿选择已参与的订单，或是距离最晚发车时间不足1分钟的车次");
        BatchDTO batchDTO = new BatchDTO();
        batchDTO.setOrderBatchList(res);
        // 根据res.isEmpty可以决定不用判空


        // 在这里设置完Batchid
        Batch batch = new Batch()
                .setId(IdWorker.getId())
                .setUserId(userId)
                .setUserNickname(user.getNickName())
                .setUserOpenid(UserContext.get().getOpenid())
                .setUserReliabilityRating(user.getReliabilityRating())
                .setStartPlace(startPlace)
                .setEndPlace(endPlace)
                .setLatestTime(latestTime)
                .setSex(shoppingCarVo.getSex())
                .setPersonNumber(shoppingCarVo.getPersonNumber());
        batchDTO.setBatch(batch);
        return batchDTO;
    }
}