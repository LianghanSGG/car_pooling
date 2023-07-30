package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生认证表
 *
 * @author LiangHanSggg
 * @date 2023-07-28 17:04
 */
@TableName("student_certification")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentCert extends BaseEntity {

    @TableId
    Long id;

    /**
     * 用户的id
     */
    Long userId;

    /**
     * 用户的openid
     */
    String userOpenid;

    /**
     * 用户的姓名
     */
    String userName;

    /**
     * 用户提供的学号
     */
    String userSchoolId;

    /**
     * 回复者id
     */
    Long auditorId;

    /**
     * 用户图片地址
     */
    String userPic;

    /**
     * 状态: 0待审核 1通过 2未通过 3 被覆盖
     */
    Integer state;

    /**
     * 反馈，如果出现了问题
     */
    String message;
}
