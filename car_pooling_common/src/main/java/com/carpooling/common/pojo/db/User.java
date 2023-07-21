package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户表
 * @author LiangHanSggg
 * @date 2023-07-16 14:19
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("user")
public class User extends BaseEntity {

    /**
     * 用户id
     */
    @TableId
    Long id;

    /**
     * 学号
     */
    String schoolId;

    /**
     * 真实姓名
     */
    String name;

    /**
     * 昵称
     */
    String nickName;


    /**
     * 电话
     */
    String phone;

    /**
     * 性别： 0 女 1 男
     */
    Integer sex;

    /**
     * 微信号
     */
    String account;

    /**
     * 拼单成功次数
     */
    Integer successesNumber;

    /**
     * 拼单总次数
     */
    Integer totalNumber;

    /**
     * 信誉度
     */
    Integer reliabilityRating;

    /**
     * 等待时间
     */
    Integer waitTime;

    String openid;

    String sessionKey;


}
