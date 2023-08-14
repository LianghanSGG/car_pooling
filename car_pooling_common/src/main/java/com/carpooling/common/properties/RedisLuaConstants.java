package com.carpooling.common.properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * @author LiangHanSggg
 * @date 2023-08-11 16:59
 */
public class RedisLuaConstants {

    public static final DefaultRedisScript<Long> luaScript;

    static {
        luaScript = new DefaultRedisScript<>();
        luaScript.setLocation(new ClassPathResource("preventDuplication.lua"));
        luaScript.setResultType(Long.class);
    }
}
