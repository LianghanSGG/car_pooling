package com.carpooling.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.mapper.OrderBatchMapper;
import com.carpooling.common.pojo.db.OrderBatch;
import com.carpooling.common.service.OrderBatchService;
import org.springframework.stereotype.Service;

/**
 * @author LiangHanSggg
 * @date 2023-08-10 14:52
 */
@Service
public class OrderBatchServiceImpl extends ServiceImpl<OrderBatchMapper, OrderBatch> implements OrderBatchService {
}
