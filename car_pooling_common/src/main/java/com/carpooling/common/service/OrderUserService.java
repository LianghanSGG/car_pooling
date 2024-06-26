package com.carpooling.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carpooling.common.pojo.db.OrderUser;
import com.carpooling.common.pojo.vo.OrderDetailVO;

import java.util.Map;

/**
 * @author LiangHanSggg
 * @date 2023-07-25 19:52
 */
public interface OrderUserService extends IService<OrderUser> {
    Map<String,Object> getHistory(int index, int size);

    OrderDetailVO getHistoryDetail(Long orderId);
}
