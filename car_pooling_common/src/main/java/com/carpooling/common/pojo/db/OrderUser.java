package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 订单、用户关系表
 * 可以用于查询历史记录
 * 实际上这个表可以和passenger合并。
 * 但是因为passenger数据出入的程度会比这个表大，多方面考虑下再建这一张表，这张表的数据只会在订单完成之后进行写入。
 * 如果后期passenger数据变化速度不大可将俩表合并。
 *
 * @author LiangHanSggg
 * @date 2023-07-16 15:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("order_user")
public class OrderUser extends BaseEntity {

    @JsonIgnore
    @TableId
    Long id;


    /**
     * 用户id
     */
    @JsonIgnore
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
    LocalDate appointmentTime;

    /**
     * 状态 0拼主 1加入者
     */
    Integer state;

}
