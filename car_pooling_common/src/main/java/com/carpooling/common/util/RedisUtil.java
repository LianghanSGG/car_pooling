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
     * String类型的减少
     *
     * @param key Redis Key
     * @param nu  要增长的值
     */
    public void StringDecrement(String key, long nu) {
        redisTemplate.opsForValue().decrement(key, nu);
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
     * 将元素添加进无限时长Set
     *
     * @param key   Redis Key
     * @param value 值，可多个
     */
    public void SetAddNoExpire(String key, Object... value) {
        redisTemplate.opsForSet().add(key, value);
    }


    /**
     * 移除Set集合中的元素
     *
     * @param key   Redis Key
     * @param value 值，可多个
     */
    public void SetDeleted(String key, Object... value) {
        redisTemplate.opsForSet().remove(key, value);
    }


    /**
     * 检查是否存在对应的ZSet
     *
     * @param key ZSet KEY
     * @return
     */
    public boolean ZSetExist(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 加入到ZSet队列
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public boolean ZSetAdd(String key, Long value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }


    /**
     * 通过分数获得ZSet中的ID
     *
     * @param key
     * @param min
     * @param max
     * @return
//     */
//    public Set<T> ZSetGetByScore(String key, double min, double max, T t) {
//        return (Set<T>) redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
//    }

    /**
     * 指定Value删除对应在ZSet中的记录
     *
     * @param key
     * @param value
     * @return
     */
    public Long ZSetDeleted(String key, Object... value) {
        return redisTemplate.opsForZSet().remove(key, value);
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


    /**
     * 使用异步加入，不会抛异常
     *
     * @param key     Redis Key
     * @param value   传入的值
     * @param timeout 时间参数
     * @param unit    时间单位
     */
    @Async("ordinaryThreadPool")
    public void AsyncStringADD(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value);
            redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.info("Redis异步添加String出现错误:方法:[{}];参数:[{},{}];异常信息:[{}]", "AsyncStringADD", key, value, e.getMessage());
        }
    }
}
