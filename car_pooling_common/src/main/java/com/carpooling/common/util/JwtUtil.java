package com.carpooling.common.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author LiangHanSggg
 * @date 2023-07-16 20:30
 */
public class JwtUtil {

    static final String KEY = "!#@Q";

    static Map<String, Object> head = new HashMap<>();

    {
        head.put("typ", "jwt");
    }

    /**
     * 从request的Header中获得jwt
     *
     * @param request
     * @return
     */
    public static String getJwtFromHttpServletRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    /**
     * 生成token
     *
     * @param key token携带的信息的Key
     * @param value token携带的信息的value
     * @param expire  过期时间,单位是毫秒
     * @return token字符串
     */
    public static String getToken(String key, Long value, long expire) {
        JWTCreator.Builder builder = JWT.create().
                withHeader(head)
                .withIssuedAt(new Date())
                .withClaim(key, value);

        return builder.withExpiresAt(new Date(System.currentTimeMillis() + expire)).sign(Algorithm.HMAC256(KEY));

    }

    public static Date getExpiresAt(String token) {
        return JWT.require(Algorithm.HMAC256(KEY)).build().verify(token).getExpiresAt();
    }

    /**
     * 检验token是否正确
     *
     * @param **token**
     * @return
     */
    public static boolean verify(String token) {
        try {

            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(KEY)).build();
            verifier.verify(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取用户自定义Claim集合
     *
     * @param token
     * @return
     */
    public static Map<String, Claim> getClaims(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(KEY)).build();
        Map<String, Claim> jwt = verifier.verify(token).getClaims();
        return jwt;
    }


    /**
     * 验证token是否失效
     *
     * @param token
     * @return true: 过期 false: 没有过期
     */
    public static boolean isExpired(String token) {
        try {
            final Date expiration = getExpiresAt(token);
            return expiration.before(new Date());
        } catch (TokenExpiredException e) {
            return true;
        }

    }

    public static void main(String[] args) throws InterruptedException {

//        Map<String, String> map = new HashMap<>();
//        map.put("key", System.currentTimeMillis() + RandomUtil.randomInt(0, 100) + "");
//        String token = JwtUtil.getToken(map, 1000000000);
//        map = new HashMap<>();
//        map.put("key", System.currentTimeMillis() + RandomUtil.randomInt(0, 100) + "");
//        String token2 = JwtUtil.getToken(map, 1000000000);
//        System.out.println(LocalDateTime.now());
//        System.out.println(token);
//        System.out.println(token2);

    }


}
