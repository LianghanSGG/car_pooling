package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单批次关系表
 *
 * @author LiangHanSggg
 * @date 2023-07-16 16:12
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("order_batch")
public class OrderBatch extends BaseEntity {

    /**
     * 订单批次关系id
     */
    @TableId
    Long id;

    /**
     * 批次id
     */
    Long batchId;

    /**
     *  订单id
     */
    Long orderId;

    /**
     * 始发地
     */
    String startPlace;

    /**
     * 目的地
     */
    String endPlace;

    /**
     * 预约时间 某年某月某日 2023-07-09。
     */
    LocalDate appointment;

    /**
     * 最早出发时间
     */
    LocalDateTime earliest_time;

    /**
     * 最晚出发时间
     */
    LocalDateTime latest_time;

    /**
     * 状态:
     * 用户申请0,拼主确认1，拼主否定2,用户同意3,用户自主取消4, 系统取消5
     */
    Integer state;




}
