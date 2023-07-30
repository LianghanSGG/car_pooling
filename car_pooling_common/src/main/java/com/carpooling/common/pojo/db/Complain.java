package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 投诉表
 *
 * @author LiangHanSggg
 * @date 2023-07-25 16:00
 */
@TableName("complain")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Complain extends BaseEntity {

    @TableId
    Long id;

    /**
     * 投诉者id
     */
    Long userId;

    /**
     * 投诉者openid
     */
    String userOpenid;

    /**
     * 订单号
     */
    Long orderId;

    /**
     * 回复者id
     */
    Long auditorId;


    /**
     * 被投诉者id，id1;id2;id3;id4
     */
    String complainId;

    /**
     * 问题描述
     */
    String question;

    /**
     * 回复内容
     */
    String reply;

    /**
     * 是否已读 0创建 1回复 2已读
     */
    Integer accepted;


}
