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

    /**
     * 微信登录接口
     * 我们无需使用到session_key进行解密，因此直接更新
     */
    LoginVo wxLogin(String code);

    /**
     * 获得个人信息
     *
     * @param userId 用户id
     * @return
     */
    UserInfoVo getInfo(Long userId);

    /**
     * 更新个人资料
     *
     * @param userId
     * @param userInfoVo
     * @return
     */
    boolean updateInfo(Long userId, UserInfoVo userInfoVo);

    /**
     * 添加个人资料
     *
     * @param userId
     * @param userInfoVo
     * @return
     */
    boolean addInfo(Long userId, UserInfoVo userInfoVo);


    /**
     * 检查信誉分是否达到80分
     *
     * @param userId
     * @return true 到达 false 未到达
     */
    boolean checkReliability(Long userId);
}
