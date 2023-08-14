package com.carpooling.common.pojo.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 加入自动加入时需要的VO
 * <p>
 * 可以和shoppingcarVO合并。抽取公共字段。
 *
 * @author LiangHanSggg
 * @date 2023-08-12 14:34
 */
@Data
public class AutoJoinVO {

    /**
     * 性别 0男女都有 1纯女 2纯男
     */
    @NotNull(message = "性别不能为空")
    @Range(min = 0, max = 2, message = "性别限制在0、1、2")
    Integer sex;

    /**
     * 本次人数
     */
    @NotNull(message = "本次人数不能为空")
    @Range(min = 0, max = 5, message = "人数应该大于0小于等于5")
    Integer personNumber;

    /**
     * 申请的订单id,传过来的时候应该是字符，不要传Long类型
     */
    @NotNull(message = "订单不能为空")
    Long orderId;

    /**
     * 权限token
     */
    @NotNull(message = "权限token不能为空")
    @Size(min = 0, max = 40, message = "权限token有误")
    String accessToken;
}
