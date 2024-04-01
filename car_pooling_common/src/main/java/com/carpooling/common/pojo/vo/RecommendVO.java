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
 * @author LiangHanSggg
 * @date 2023-08-12 19:05
 */
@Data
public class RecommendVO {

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
     * 最早出发时间
     */
    @NotNull(message = "时间有误")
    @Future(message = "时间有误")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startTime;


    /**
     * 最晚出发时间
     */
    @NotNull(message = "时间有误")
    @Future(message = "时间有误")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime lastTime;
}
