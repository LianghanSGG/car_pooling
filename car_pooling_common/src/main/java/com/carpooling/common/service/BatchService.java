package com.carpooling.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carpooling.common.pojo.db.Batch;
import com.carpooling.common.pojo.vo.BatchVO;
import com.carpooling.common.pojo.vo.OrderBriefInfoVO;
import com.carpooling.common.pojo.vo.ShoppingCarVo;
import com.carpooling.common.pojo.vo.UserSimpleInfoVO;

import java.util.List;

/**
 * @author LiangHanSggg
 * @date 2023-08-02 20:46
 */
public interface BatchService extends IService<Batch> {

    /**
     * 获得批次
     *
     * @return
     */
    List<BatchVO> getBatchList();


    /**
     * 根据列表创建创建批次和对应的记录，自动加入的不是使用这个方法
     *
     * @param shoppingCarVo
     * @return
     */
    String createBatch(ShoppingCarVo shoppingCarVo);


    /**
     * 对订单列表进行收集
     * 有的话直接走redis。没有的话去查mysql。然后异步的写入到redis中。
     *
     * @param orderList
     * @return
     */
    List<OrderBriefInfoVO> orderCollect(Long[] orderList);

    /**
     * 检查是否有乘客申请
     */
    List<UserSimpleInfoVO> listRequest();

    /**
     * 允许用户通过
     * @param orderBatchId
     * @return
     */
    boolean passReq(Long orderBatchId);

    /**
     * 拒绝用户的请求
     * @param orderBatchId
     * @return
     */
    boolean refuseReq(Long orderBatchId);
}
