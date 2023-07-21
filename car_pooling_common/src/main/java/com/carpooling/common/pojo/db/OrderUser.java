package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 订单、用户关系表
 * 可以用于查询历史记录
 *
 * @author LiangHanSggg
 * @date 2023-07-16 15:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("order_user")
public class OrderUser extends BaseEntity {

    @TableId
    Long id;


    /**
     * 用户id
     */
    Long userId;


    /**
     * 订单id
     */
    Long orderId;

    /**
     * 出发地
     */
    String startPlace;

    /**
     * 目的地
     */
    String endPlace;

    /**
     * 出发的日期
     */
    LocalDate  appointmentTime;

    /**
     * 状态 0拼主 1加入者
     */
    Integer state;

}
