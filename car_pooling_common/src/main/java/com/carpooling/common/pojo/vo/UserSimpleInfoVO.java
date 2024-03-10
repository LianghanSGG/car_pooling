package com.carpooling.common.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简单展示用户的接口，用于在扫批次的时候返回
 *
 * @author LiangHanSggg
 * @date 2024-03-10 16:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSimpleInfoVO {
    /**
     * 学生名
     */
    String userName;

    /**
     * 学号
     */
    String schoolId;

    /**
     * 订单批次对应表的id
     */
    String orderBatchId;
}
