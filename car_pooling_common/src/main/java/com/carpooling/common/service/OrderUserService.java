package com.carpooling.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carpooling.common.pojo.db.OrderUser;

import java.util.List;

/**
 * @author LiangHanSggg
 * @date 2023-07-25 19:52
 */
public interface OrderUserService extends IService<OrderUser> {
    List<OrderUser> getHistory(int index, int size);
}
