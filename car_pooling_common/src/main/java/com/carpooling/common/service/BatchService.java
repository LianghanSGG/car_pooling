package com.carpooling.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carpooling.common.pojo.db.Batch;
import com.carpooling.common.pojo.vo.BatchVO;
import com.carpooling.common.pojo.vo.ShoppingCarVo;

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
     * @param shoppingCarVo
     * @return
     */
    String createBatch(ShoppingCarVo shoppingCarVo);

}
