package com.carpooling.common.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.carpooling.common.pojo.db.User;
import com.carpooling.common.pojo.vo.LoginVo;
import com.carpooling.common.pojo.vo.UserInfoVo;


/**
 * @author LiangHanSggg
 * @date 2023-07-16 20:11
 */
public interface UserService extends IService<User> {

    User checkUserState(Long userId);

    LoginVo wxLogin(String code);

    UserInfoVo getInfo(Long userId);

    boolean updateInfo(Long userId, UserInfoVo userInfoVo);

    boolean addInfo(Long userId, UserInfoVo userInfoVo);
}
