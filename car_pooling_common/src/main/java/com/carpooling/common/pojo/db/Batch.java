package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 批次表
 *
 * @author LiangHanSggg
 * @date 2023-07-16 17:39
 */
@TableName("batch")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Batch extends BaseEntity {

    /**
     * 批次id
     */
    @TableId(type = IdType.INPUT)
    Long id;

    /**
     * 用户id
     */
    Long userId;

    /**
     * 用户昵称
     */
    String userNickname;

    /**
     * 用户openid
     */
    String userOpenid;

    /**
     * 用户信誉度
     */
    Integer userReliabilityRating;

    /**
     * 出发地
     */
    String startPlace;

    /**
     * 目的地
     */
    String endPlace;

    /**
     * 批次状态 0创建 1结束 2取消
     */
    Integer state;

    /**
     * 成功加入的订单id，冗余字段，不一定有
     */
    Long succeedOrderId;


    /**
     * 批次中最晚出发时间 可以最后做通知用的
     */
    LocalDateTime latestTime;

    /**
     * 性别 0男女都有 1纯女 2纯男
     */
    Integer sex;

    /**
     * 本次人数
     */
    Integer personNumber;


}
