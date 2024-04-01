package com.carpooling.common.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.springframework.lang.Nullable;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 订单条件查询VO
 *
 * @author LiangHanSggg
 * @date 2023-08-03 15:53
 */
@Data
public class OderListConditionVO {

    /**
     * 下标页
     */
    @NotNull(message = "下表页不能为空")
    @Range(min = 1, max = 50, message = "下标页应该是1-50")
    Integer index;

    /**
     * 展示条数
     */
    @NotNull(message = "展示条数不能为空")
    @Range(min = 1, max = 10, message = "展示条数应该在1-10")
    Integer page;

    /**
     * 出发地
     */
    @NotBlank(message = "出发地不能为空")
    @Size(min = 2, max = 15, message = "出发地长度不能超过15")
    String startPlace;

    /**
     * 目的地
     */
    @NotBlank(message = "目的地不能为空")
    @Size(min = 2, max = 15, message = "目的地长度不能超过15")
    String endPlace;

    /**
     * 出发时间
     */
    @NotNull(message = "出发时间不能为空")
    @Future(message = "时间有误")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startTime;

    /**
     * 所需人数
     */
    @Nullable
    @Range(min = 1, max = 5, message = "所需人数")
    Integer alreadyNumber;


    /**
     * 性别限制：0 无限制 ，1 只限女 2 只限男
     */
    @NotNull(message = "性别限制不能为空")
    @Range(min = 0, max = 2, message = "性别限制在0、1、2")
    Integer sex;


}
