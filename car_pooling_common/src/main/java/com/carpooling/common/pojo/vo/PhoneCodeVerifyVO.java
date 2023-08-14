package com.carpooling.common.pojo.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 电话验证码对象
 *
 * @author LiangHanSggg
 * @date 2023-08-05 16:50
 */
@Data
public class PhoneCodeVerifyVO {

    /**
     * 电话号
     */
    @NotBlank(message = "手机号码不能为空")
    @NotNull(message = "手机号不能为空")
    @Length(min = 11, max = 11, message = "手机号只能为11位")
    @Pattern(regexp = "^[1][3,4,5,6,7,8,9][0-9]{9}$", message = "手机号格式有误")
    String phone;

    /**
     * 验证码。不是验证的时候不要带
     */
    @Length(min = 6, max = 6, message = "验证码长度应为6位")
    String code;
}
