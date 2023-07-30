package com.carpooling.common.pojo.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 学生认证VO
 *
 * @author LiangHanSggg
 * @date 2023-07-28 19:28
 */
@Data
public class StuCertVO {

    /**
     * 学生输入的姓名
     */
    @NotNull(message = "姓名不能为空")
    @Size(max = 40, message = "姓名长度过长")
    String userName;

    /**
     * 学生输入的学号
     */
    @NotNull(message = "学号不能为空")
    @Size(max = 12, message = "长度过长")
    String userSchoolId;

    /**
     * 上传的图片的地址
     */
    @NotNull(message = "图片不能为空")
    @Size(max = 200, message = "图片地址过长")
    String userPic;

}
