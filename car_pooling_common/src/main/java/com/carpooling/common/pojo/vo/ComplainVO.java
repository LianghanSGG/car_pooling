package com.carpooling.common.pojo.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 投诉VO
 *
 * @author LiangHanSggg
 * @date 2023-07-25 16:12
 */
@Data
public class ComplainVO {

    /**
     * 订单号
     */
    @NotNull(message = "订单id不能为空")
    String orderId;

    /**
     * 投诉者的id id1;id2;id3;id4  注意最后是没有;
     */
    @Size(min = 0, max = 800, message = "id数不可过多")
    String complainId;

    /**
     * 问题描述
     */
    @NotBlank(message = "问题描述不能为空")
    @Size(min = 4, max = 2000, message = "问题描述字数应该大于4小于2000")
    String question;


}
