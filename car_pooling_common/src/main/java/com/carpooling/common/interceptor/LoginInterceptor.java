package com.carpooling.common.interceptor;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.interfaces.Claim;
import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.db.User;
import com.carpooling.common.pojo.vo.UserVO;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.properties.NumberConstants;
import com.carpooling.common.service.UserService;
import com.carpooling.common.util.HttpUtil;
import com.carpooling.common.util.JwtUtil;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 正常用户的token有效期是7天， redis会缓存14天.
 * <p>
 * 对于临时状态的token会保持 15天，除非是15内一直没有登录
 * <p>
 * 可能存在的性能瓶颈：
 * 存在TheadLocal中的对象过于大了。决定：将信息进行拆分。如果userContext只存角色、IP，userId,openId。剩下的作为个人信息模板，
 * 如果机器性能跟得上可以将更多的信息存在TheadLocal，减少对于Redis的访问。但是注意内存问题。
 * <p>
 * <p>
 * 少部分代码重复，个人认为是无需抽取公共方法，抽取之后需要创建新的数据结构存储返回值，而且还要增加一个栈帧。
 * <p>
 * Map的initSize=2 符合初始大小和负载因子和业务的要求，要么直接换数据结构不得修改大小。
 * <p>
 * 第二版可以考虑将返回值的Map修改一下，毕竟创建map还是需要牺牲一些性能。
 *
 * @author LiangHanSggg
 * @date 2023-07-16 17:47
 */
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    HttpUtil hp;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserService userServiceImpl;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = JwtUtil.getJwtFromHttpServletRequest(request);

        if (StrUtil.isBlankIfStr(token)) {
            hp.response(response, false, 403, "未登录", null);
            return false;
        }


        boolean expired = false;

        try {
            expired = JwtUtil.isExpired(token);
        } catch (Exception e) {
            hp.response(response, false, 400, "token格式有误", null);
            return false;
        }


        String key = token.substring(token.lastIndexOf(".") + 1);
        UserVO userVO = null;

        // 先判断大多情况，减少开销
        if (!expired) {

            Map<String, Claim> claims = JwtUtil.getClaims(token);
            if (claims.containsKey("temp")) {

                Long userId = claims.get("temp").asLong();


                if (Objects.isNull(userId)) {
                    hp.response(response, false, 400, "token格式有误", null);
                    return false;
                }


                User dbUser = userServiceImpl.checkUserState(userId);

                if (dbUser == null) {
                    hp.response(response, false, 403, "未登录", null);
                    return false;
                }

                //判断是否已经更新了状态
                if (!StrUtil.isEmptyIfStr(dbUser.getSchoolId()) && !StrUtil.isEmptyIfStr(dbUser.getPhone())) {

                    String token1 = JwtUtil.getToken("key", RandomUtil.randomLong(Long.MAX_VALUE), NumberConstants.TOKEN_USER_7_DAY);

                    String redisKey = RedisPrefix.USER + token1.substring(token1.lastIndexOf(".") + 1);

                    userVO = new UserVO();

                    userVO.setId(dbUser.getId());
                    userVO.setOpenid(dbUser.getOpenid());

                    redisUtil.StringAdd(redisKey, userVO, 14, TimeUnit.DAYS);


                    Map<String, String> map2 = new HashMap<>(2);
                    map2.put("token", token1);


                    hp.response(response, true, 201, null, map2);
                    return false;

                } else {
                    userVO = new UserVO();

                    boolean asc = StrUtil.isEmptyIfStr(dbUser.getSchoolId());

                    if (asc && StrUtil.isEmptyIfStr(dbUser.getPhone())) {
                        userVO.setState(4);
                    } else if (asc) {
                        userVO.setState(3);
                    } else {
                        userVO.setState(2);
                    }

                    userVO.setClientIP(hp.getClientIP(request));
                    UserContext.set(userVO);

                    return true;
                }


            } else {
                userVO = redisUtil.StringGet(RedisPrefix.USER + key, UserVO.class);

                if (userVO == null) {
                    hp.response(response, false, 403, "未登录", null);
                    return false;
                } else {
                    // 正常状态
                    userVO.setState(0);
                    userVO.setClientIP(hp.getClientIP(request));
                    UserContext.set(userVO);
                    return true;
                }
            }


        } else {

            userVO = redisUtil.StringGet(RedisPrefix.USER + key, UserVO.class);

            if (userVO == null) {
                // 过期并且redis为空。 要么是临时状态保存太久过期了，要么是太久没有登录。
                hp.response(response, false, 403, "未登录", null);
            } else {
                // 处于刷新期

                String token1 = JwtUtil.getToken("key", RandomUtil.randomLong(Long.MAX_VALUE), NumberConstants.TOKEN_USER_7_DAY);

                String redisKey = RedisPrefix.USER + token1.substring(token1.lastIndexOf(".") + 1);

                redisUtil.StringAdd(redisKey, userVO, 14, TimeUnit.DAYS);


                Map<String, String> map2 = new HashMap<>(2);

                map2.put("token", token1);

                redisUtil.AsyncDeleted(RedisPrefix.USER + key);

                hp.response(response, true, 201, null, map2);

            }
            return false;
        }

    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove();
        return;
    }


}
