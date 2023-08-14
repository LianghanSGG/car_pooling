package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单批次关系表
 * 一些冗余字段感觉可以省略掉，第二版可以根据实际情况进行修改 2023-08-07
 *
 * orderBatch以后是最有可能需要分表的。24小时3次机会，一次5条记录。一人一天至多15条。
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
     * 订单id
     */
    Long orderId;

    /**
     * 拥有者的id
     */
    Long ownerId;

    /**
     * 状态:
     * 用户申请0,拼主确认1，拼主否定2,用户自主取消3,系统取消4,
     */
    Integer state;


}
