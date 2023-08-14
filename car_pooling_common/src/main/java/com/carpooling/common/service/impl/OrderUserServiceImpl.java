package com.carpooling.common.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.mapper.OrderUserMapper;
import com.carpooling.common.pojo.db.OrderUser;
import com.carpooling.common.service.OrderUserService;
import com.carpooling.common.util.UserContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author LiangHanSggg
 * @date 2023-07-25 19:53
 */
@Service
public class OrderUserServiceImpl extends ServiceImpl<OrderUserMapper, OrderUser> implements OrderUserService {

    @Override
    public List<OrderUser> getHistory(int index, int size) {
        Long userId = UserContext.get().getId();
        Page<OrderUser> p = new Page<>(index, size);
        page(p, Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getUserId, userId)
                .ne(OrderUser::getState, 0));
        return p.getRecords();
    }


}
