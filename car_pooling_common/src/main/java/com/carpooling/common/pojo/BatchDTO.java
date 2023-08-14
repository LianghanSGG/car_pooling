package com.carpooling.common.pojo;

import com.carpooling.common.pojo.db.Batch;
import com.carpooling.common.pojo.db.OrderBatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author LiangHanSggg
 * @date 2023-08-08 19:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BatchDTO {

    Batch batch;

    List<OrderBatch> orderBatchList;
}
