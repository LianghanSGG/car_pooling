package com.carpooling.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.exception.DException;
import com.carpooling.common.mapper.UserMapper;
import com.carpooling.common.pojo.WxLoginEntity;
import com.carpooling.common.pojo.db.User;
import com.carpooling.common.pojo.vo.LoginVo;
import com.carpooling.common.pojo.vo.UserInfoVo;
import com.carpooling.common.pojo.vo.UserVO;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.properties.NumberConstants;
import com.carpooling.common.service.UserService;
import com.carpooling.common.util.JwtUtil;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.WxUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author LiangHanSggg
 * @date 2023-07-16 20:11
 */
@Slf4j
@Service
public class userServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private WxUtil wxUtil;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public User checkUserState(Long userId) {
        return getOne(Wrappers.lambdaQuery(User.class).eq(User::getId, userId).select(User::getPhone, User::getSchoolId, User::getOpenid, User::getId).last("limit 1"));
    }

    /**
     * 我们无需使用到session_key进行解密，因此直接更新
     *
     * @param code
     * @return
     */
    @Override
    public LoginVo wxLogin(String code) {
        WxLoginEntity wxLoginEntity = null;

        try {
            wxLoginEntity = wxUtil.wxLogin(code);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (Objects.isNull(wxLoginEntity)) {
            throw new RuntimeException("wxLoginEntity IS NULL");
        }

        if (wxLoginEntity.getErrcode() == 40029) {
            throw new DException("code超时");
        }

        LoginVo loginVo = new LoginVo();

        User user = getOne(Wrappers.lambdaQuery(User.class).eq(User::getOpenid, wxLoginEntity.getOpenid()));
        if (Objects.isNull(user)) {

            user = new User();
            user.setOpenid(wxLoginEntity.getOpenid());
            user.setSessionKey(wxLoginEntity.getSession_key());
            user.setReliabilityRating(100);

            save(user);

            String token = JwtUtil.getToken("temp", user.getId(), NumberConstants.TOKEN_TIME_15_DAY);

            loginVo.setState(4);
            loginVo.setToken(token);

        } else {


            boolean p = StrUtil.isEmptyIfStr(user.getPhone());
            boolean a = StrUtil.isEmptyIfStr(user.getAccount());
            if (!p & !a) {
                //正常用户
                loginVo.setState(0);
                loginVo.setNickName(user.getNickName());
                loginVo.setPhone(DesensitizedUtil.mobilePhone(user.getPhone()));

                String token = JwtUtil.getToken("key", RandomUtil.randomLong(Long.MAX_VALUE), NumberConstants.TOKEN_USER_7_DAY);

                String redisKey = RedisPrefix.USER + token.substring(token.lastIndexOf(".") + 1);

                UserVO userVO = new UserVO();
                userVO.setId(user.getId());
                userVO.setOpenid(user.getOpenid());

                redisUtil.StringAdd(redisKey, userVO, 14, TimeUnit.DAYS);

                loginVo.setToken(token);

            } else {

                if (p && a) {
                    loginVo.setState(1);
                } else if (!p) {
                    loginVo.setState(3);
                } else {
                    loginVo.setState(2);
                }
                String token = JwtUtil.getToken("temp", user.getId(), NumberConstants.TOKEN_TIME_15_DAY);
                loginVo.setToken(token);

            }

        }
        return loginVo;


    }

    /**
     * 获得个人信息
     *
     * @param userId 用户id
     * @return
     */
    @Override
    public UserInfoVo getInfo(Long userId) {
        User user = getOne(Wrappers.lambdaQuery(User.class).eq(User::getId, userId));
        if (Objects.isNull(user)) return null;

        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtil.copyProperties(user, userInfoVo, "id", "openid", "sessionKey", "deleted", "createTime", "updateTime");
        return userInfoVo;
    }

    /**
     * 更新个人资料
     *
     * @param userId
     * @param userInfoVo
     * @return
     */
    @Override
    public boolean updateInfo(Long userId, UserInfoVo userInfoVo) {
        User user = new User();
        user.setId(userId);
        BeanUtil.copyProperties(userInfoVo, user);
        if (updateById(user)) {
            redisUtil.StringAdd(RedisPrefix.USERINFO_TIME + userId, "1", 15, TimeUnit.DAYS);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addInfo(Long userId, UserInfoVo userInfoVo) {
        User user = new User();
        user.setId(userId);
        BeanUtil.copyProperties(userInfoVo, user);
        return updateById(user);
    }


}
