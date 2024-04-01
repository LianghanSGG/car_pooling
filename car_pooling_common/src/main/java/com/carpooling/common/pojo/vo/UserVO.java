package com.carpooling.common.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户状态VO
 * 应用：Redis、登录拦截器。
 *
 * @author LiangHanSggg
 * @date 2023-07-17 15:25
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserVO {

    /**

     *         // 应该有三个状态。 没有注册个人信息、没有注册电话号、没有实名认证
     *         // 重新规定 0 是正常 剩下的按照顺序下去
     * 无需序列化到DB
     */
    @JsonIgnore
    Integer state;

    /**
     * 登录的IP地址，不应该序列化到DB
     */
    @JsonIgnore
    String clientIP;


    /**
     * 用户id
     */
    Long id;


    /**
     * openid
     */
    String openid;


}
