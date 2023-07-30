package com.carpooling.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存工具
 *
 * @author LiangHanSggg
 * @date 2023-07-18 16:17
 */
@Component
@Slf4j
public class RedisUtil {

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 检查value是否存在
     *
     * @param key
     * @return
     */
    public boolean ValueExist(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * @param key     Redis Key
     * @param value   传入的值
     * @param timeout 时间参数
     * @param unit    时间单位
     */
    public void StringAdd(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, timeout, unit);
    }

    /**
     * @param key 要查询的Key
     * @param t   得到的数据类型
     * @param <T>
     * @return
     */
    public <T> T StringGet(String key, Class<T> t) {
        return ((T) redisTemplate.opsForValue().get(key));
    }

    /**
     * String类型的增长
     *
     * @param key Redis Key
     * @param nu  要增长的值
     */
    public void StringIncrement(String key, long nu) {
        redisTemplate.opsForValue().increment(key, nu);
    }

    /**
     * 检查是否存在于Set
     *
     * @param key
     * @param value
     * @return
     */
    public boolean SetExistMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 将元素添加进Set
     *
     * @param key     Redis Key
     * @param timeout 时长
     * @param unit    单位
     * @param value   值，可多个
     */
    public void SetAdd(String key, long timeout, TimeUnit unit, Object... value) {
        redisTemplate.opsForSet().add(key, value);
        redisTemplate.expire(key, timeout, unit);
    }


    /**
     * 使用异步进行删除，不会抛出异常
     *
     * @param key
     */
    @Async("ordinaryThreadPool")
    public void AsyncDeleted(String key) {

        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.info("Redis删除出现错误:方法:[{}];参数:[{}];异常信息:[{}]", "AsyncDeleted", key, e.getMessage());
        }
    }


}
