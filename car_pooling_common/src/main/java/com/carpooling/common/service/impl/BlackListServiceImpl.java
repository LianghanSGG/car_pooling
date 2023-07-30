package com.carpooling.common.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.mapper.BlackListMapper;
import com.carpooling.common.pojo.db.BlackList;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.service.BlackListService;
import com.carpooling.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用于关键地方配合权限验证是否完整一起判断是否有资格进行下单
 *
 * @author LiangHanSggg
 * @date 2023-07-25 19:03
 */
// TODO: 2023/7/25 还未测试
@Service
public class BlackListServiceImpl extends ServiceImpl<BlackListMapper, BlackList> implements BlackListService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * @param userId
     * @return false不存在 true存在
     */
    @Override
    public boolean checkExist(Long userId) {
        if (redisUtil.ValueExist(RedisPrefix.BLACKLIST)) {
            return redisUtil.SetExistMember(RedisPrefix.BLACKLIST, userId);
        }

        List<BlackList> list = list();
        if (list == null || list.size() == 0) {
            return false;
        }
        int day = RandomUtil.randomInt(14, 32);
        boolean flag = false;
        int length = list.size();
        for (int i = 0; i < length; i++) {
            if (!flag) {
                flag = list.get(i).equals(userId);
            }
            redisUtil.SetAdd(RedisPrefix.BLACKLIST, day, TimeUnit.DAYS, list.get(i).getUserId());
        }

        return flag;
    }
}
