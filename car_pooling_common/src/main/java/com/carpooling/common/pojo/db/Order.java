package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单表
 * 下个版本可以把最早出发时间和最晚出发时间改成Integer类型，有了appointmentTime可以忽略时间
 * 注意，我用了mybatisplus的@version注解，有些时候没必要自旋。
 * @author LiangHanSggg
 * @date 2023-07-16 16:18
 */
@TableName("car_order")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Order extends BaseEntity {

    /**
     * 订单id
     */
    @TableId(type = IdType.INPUT)
    Long id;

    /**
     * 创建人id
     */
    Long createPersonId;

    /**
     * 拥有者id
     */
    Long ownerId;

    /**
     * 拥有者的openid
     */
    String ownerOpenid;

    /**
     * 订单状态 0创建，1完成，2等待取消，3取消。
     */
    Integer state;

    /**
     * 始发地
     */
    String startPlace;

    /**
     * 目标地
     */
    String endPlace;

    /**
     * 出发的日期，没有具体时间。2023-07-09
     */
    LocalDate appointmentTime;

    /**
     * 最早出发时间
     */
    LocalDateTime earliestTime;

    /**
     * 最晚出发时间
     */
    LocalDateTime latestTime;

    /**
     * 现有人数
     */
    Integer alreadyNumber;

    /**
     * 所需拼单的人数
     */
    Integer targetNumber;

    /**
     * 性别限制：0 无限制 ，1 只限女 2 只限男
     */
    Integer sex;

    /**
     * 是否自动加入 0不自动 1自动
     */
    Integer autoJoin;

    /**
     * 版本号
     */
    @Version
    Integer version;


}
