package com.carpooling.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carpooling.common.pojo.db.Order;
import com.carpooling.common.pojo.vo.*;

import java.util.List;

/**
 * @author LiangHanSggg
 * @date 2023-08-03 14:14
 */
public interface OrderService extends IService<Order> {


    /**
     * 用于获得首页的订单列表
     *
     * @return
     */
    List<OrderInfoVO> orderList(int index, int size);

    /**
     * 有筛选条件的获得首页订单列表
     *
     * @param condition
     * @return
     */
    List<OrderInfoVO> conditionOrder(OderListConditionVO condition);

    /**
     * 获得上传的token，解决重复提交问题
     * <p>
     * 我的实现方案：key是前缀加id。v 是uuid。这样就可以不用做次数校验。
     * 第二版可以做一下请求限制
     *
     * @return token
     */
    String getToken();


    /**
     * 创建订单
     *
     * @param placeOrderVO
     * @return
     */
    String createOrder(PlaceOrderVO placeOrderVO);


    /**
     * 用户直接加入自动加入的订单
     * <p>
     * 在大并发的时候数据库的CAS会导致Mysql的CPU打满
     *
     * @param autoJoinVO
     * @return
     */
    String authJoin(AutoJoinVO autoJoinVO);


    /**
     * 获得推荐的列表
     *
     * @param recommendVO
     * @return
     */
    List<OrderInfoVO> getRecommended(RecommendVO recommendVO);

    /**
     * 获得订单详情
     *
     * @return
     */
    List<OrderDetailVO> getOrderDetail();

    /**
     * 获得自己成功加入的订单
     *
     * @return
     */
    List<OrderBriefInfoVO> getSelf();

    /**
     * 获得加入订单的详情。只能看到自己和团长的信息，其他的都是脱敏
     * <p>
     * 可以和获得订单详情合并成一个方法
     *
     * @return
     */
    OrderDetailVO JoinOrderDetail(Long orderId);


    /**
     * 用户取消自己加入的单子
     *
     * @param orderId
     * @return
     */
    boolean cancelOrder(Long orderId);


    /**
     * 取消自建单
     * 直接解散。异步通知群友
     *
     * @param orderId
     * @return
     */
    boolean leaderCancelOrder(Long orderId);

    /**
     * 团长完成订单
     *
     * @param orderId
     * @return
     */
    boolean leaderCompleteOrder(Long orderId);
}
