package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 订单、用户关系表
 * 这个表充当乘客、历史订单俩个职责
 * 拼主应当在创建完订单的时候在这个表中也创建一条记录：
 * 如果不创建。那么检索历史订单的时候需要去order表和这张表俩张一起检索
 * 创建，只需要多加一行记录，可以减少查询。但是要使用事务对俩张表中的记录的保证一致性。
 * <p>
 * <p>
 * 建议订单id和用户id简历联合索引
 *
 * @author LiangHanSggg
 * @date 2023-07-16 15:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("order_user")
@Accessors(chain = true)
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
     * 用户姓名
     */
    String userName;

    /**
     * 批次中的性别 0 男女都有 1纯女 1纯男
     */
    Integer userSex;

    /**
     * 本次人数
     */
    Integer personNumber;

    /**
     * 用户电话号
     */
    String userPhone;

    /**
     * 用户的微信号
     */
    String userWechatAccount;

    /**
     * 用户的openid
     */
    String userOpenid;

    /**
     * 用户的角色状态:0 拼主 1加入者
     */
    Integer userRole;

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
     * 状态 0加入 1结束 2退出 3被t
     */
    Integer state;


}
