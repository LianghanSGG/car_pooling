package com.carpooling.common.pojo.vo;

import lombok.Data;

/**
 * @author LiangHanSggg
 * @date 2023-08-02 20:48
 */
@Data
public class BatchVO {

    /**
     * 批次id
     */
    Long batchId;

    /**
     * 起始地
     */
    String startPlace;

    /**
     * 目的地
     */
    String endPlace;


}
