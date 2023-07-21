package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成员表
 * 本质上和订单用户表性质相同，但是可以拆分职责
 *
 * @author LiangHanSggg
 * @date 2023-07-16 14:30
 */
@TableName("passenger")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Passenger extends BaseEntity {

    /**
     * 用户id
     */
    @TableId
    Long id;

    /**
     * 订单id
     */
    Long orderId;


    /**
     * 用户id
     */
    Long userId;

    /**
     * 用户姓名
     */
    String userName;

    /**
     * 用户姓名 0女 1男
     */
    Integer user_sex;

    /**
     * 用户电话
     */
    String userPhone;

    /**
     * 用户微信号
     */
    String userWechatAccount;

    /**
     * 状态:0加入，1退出，2被t
     */
    Integer state;


}
