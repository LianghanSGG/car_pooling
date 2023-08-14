package com.carpooling.common.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 创建订单的Order信息对象
 * <p>
 * 数字字段综合成一个数组，减少传输的大小，下标代表含义。
 *
 * @author LiangHanSggg
 * @date 2023-08-11 16:18
 */
@Data
public class PlaceOrderVO {

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
     * 最早出发时间
     */
    @NotNull(message = "最早出发时间不能为空")
    @Future(message = "最早出发时间应该晚于现在")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime earliestTime;

    /**
     * 最晚出发时间
     */
    @NotNull(message = "最晚出发时间不能为空")
    @Future(message = "最晚出发时间应该晚于现在")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime latestTime;

    /**
     * 现有人数
     */
    @NotNull(message = "现有人数不能为空")
    @Range(min = 1, max = 7, message = "现有人数不得超过7")
    Integer alreadyNumber;

    /**
     * 所需拼单的人数
     */
    @NotNull(message = "所需人数不能为空")
    @Range(min = 1, max = 7, message = "所需人数不能超过7")
    Integer targetNumber;

    /**
     * 性别限制：0无限制、1只限女、2只限男
     */
    @NotNull(message = "性别限制不能为空")
    @Range(min = 0, max = 2, message = "性别限制在0、1、2")
    Integer sex;

    /**
     * 是否自动加入 0不自动 1自动加入
     */
    @NotNull(message = "自动加入不能为空")
    @Range(min = 0, max = 1, message = "自动加入限制0,1")
    Integer autoJoin;

    /**
     * 权限token
     */
    @NotNull(message = "权限token不能为空")
    @Size(min = 0, max = 40, message = "权限token有误")
    String accessToken;
}
