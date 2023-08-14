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
     * 第二版可以做一下请求限制，不然如果一直以添加的形式加入到redis会被打爆。
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
}
