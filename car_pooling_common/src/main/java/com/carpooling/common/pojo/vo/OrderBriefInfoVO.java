package com.carpooling.common.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单简略信息VO 不包含任何可变的状态
 *
 * @author LiangHanSggg
 * @date 2023-08-07 22:12
 */
@Data
public class OrderBriefInfoVO {

    /**
     * 订单id
     */
    Long id;

    /**
     * 拥有者id
     */
    Long ownerId;


    /**
     * 出发地
     */
    String startPlace;

    /**
     * 目的地
     */
    String endPlace;

    /**
     * 最早出发时间
     */
    LocalDateTime earliestTime;

    /**
     * 最晚出发时间
     */
    LocalDateTime latestTime;

    /**
     * 自动加入 0 不自动 1自动加入
     */
    Integer autoJoin;

}
