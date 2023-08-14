package com.carpooling.common.service.impl;

import com.carpooling.common.exception.OrderVerifyException;
import com.carpooling.common.exception.serviceLogic;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.properties.RedisLuaConstants;
import com.carpooling.common.service.CommonService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author LiangHanSggg
 * @date 2023-08-11 17:04
 */
@Slf4j
@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Integer checkAndQualificationCancel(Long userId, String redisKey, String msg, String accessToken) {
        // 前置次数判断。

        Integer qualification = redisUtil.StringGet(redisKey, Integer.class);

        if (Objects.nonNull(qualification) && qualification == 0) throw new serviceLogic(msg);


        /**
         * 拿着这个token去redis中进行查看
         * redis中没有userid。已经被使用了。重复提交---结束
         * redis中有userid，拿出来比较。 错误---记录下来，表示重复提交
         * --正确！ 进行删除。
         */

        ArrayList<String> list = new ArrayList<>();
        list.add(RedisPrefix.ORDER_ACCESS + userId);
        Long res = (Long) redisTemplate.execute(RedisLuaConstants.luaScript, list, accessToken);
        if (res == 0) {
            throw new OrderVerifyException("已经提交订单请勿重复提交");
        }
        if (res == 2) {
            log.error("使用过期的token进行验证userid:{},userIP:{}", userId, UserContext.get().getClientIP());
            throw new OrderVerifyException("违法记录已经被记录下来");
        }
        if (res == 3) {
            throw new RuntimeException("Redis删除失败");
        }

        return qualification;
    }

    @Override
    public void afterPlaceOrder(Integer qualification, String redisKey, int num, int time) {
        // 根据redis是否存在进行判断
        // 理论上这里得再去查一次。看看qualification是否存在。如果存在则自减。
        // 极端情况： 距离redis过期时间短于我们前置到后置的时间。这样就会导致走到else。 坏处：多了一次下单的机会。
        if (Objects.isNull(qualification)) {
            redisUtil.StringAdd(redisKey, num, time, TimeUnit.HOURS);
        } else {
            redisUtil.StringDecrement(redisKey, 1);
        }
    }
}
