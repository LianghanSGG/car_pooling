package com.carpooling.common.pojo.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 批次表VO
 * 这里用于加入，而不是用户创建自己的订单
 * 一个用户发起一个发车请求，可以选多个车次.发车请求就是Batch。车次就是orderList。
 *
 * @author LiangHanSggg
 * @date 2023-08-07 14:47
 */
@Data
public class ShoppingCarVo {

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
     * 申请的订单id,传过来的时候应该是字符数组，不要传Long类型数组
     */
    @NotNull(message = "订单数组不能为空")
    @Size(min = 1, max = 3, message = "申请的订单数量应该大于1小于等于3")
    Long[] orderList;

    /**
     * 权限token
     */
    @NotNull(message = "权限token不能为空")
    @Size(min = 0, max = 40, message = "权限token有误")
    String accessToken;
}
