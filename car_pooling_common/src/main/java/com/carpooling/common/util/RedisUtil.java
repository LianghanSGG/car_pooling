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
