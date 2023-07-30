package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 反馈表
 *
 * @author LiangHanSggg
 * @date 2023-07-24 21:36
 */
@TableName("feedback")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FeedBack extends BaseEntity {

    @TableId
    Long id;

    /**
     * 提问者的openid
     */
    String userOpenid;

    /**
     * 回答者的id
     */
    Long auditorId;

    /**
     * 问题
     */
    String question;

    /**
     * 反馈
     */
    String reply;

    /**
     * 是否已读 0创建 1已回复 2已读
     */
    Integer accepted;

}
