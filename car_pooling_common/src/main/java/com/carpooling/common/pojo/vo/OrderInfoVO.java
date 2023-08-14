package com.carpooling.common.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 可优化的点:可以和order合并/Integer类型的数据可以合并成一个数组，减少传输
 *
 * @author LiangHanSggg
 * @date 2023-08-02 21:25
 */
@Data
@Accessors(chain = true)
public class OrderInfoVO {

    /**
     * 订单id
     */
    Long orderId;


    /**
     * 始发地
     */
    String startPlace;

    /**
     * 目的地
     */
    String endPlace;

    /**
     * 出发的日期，没有具体时间。2023-07-09
     */
    String appointmentTime;

    /**
     * 最早出发时间
     */
    String earliestTime;

    /**
     * 最晚出发时间
     */
    String latestTime;

    /**
     * 现有人数
     */
    Integer alreadyNumber;

    /**
     * 所需拼单的人数
     */
    Integer targetNumber;

    /**
     * 是否自动加入 0不自动 1自动
     */
    Integer autoJoin;

    /**
     * 性别限制：0 无限制 ，1 只限女 2 只限男
     */
    Integer sex;

    /**
     * 是否是属于自己的 0不是 1是;
     */
    Integer myself;
}
