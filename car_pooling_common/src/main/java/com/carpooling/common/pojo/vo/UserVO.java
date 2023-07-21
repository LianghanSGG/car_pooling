package com.carpooling.common.pojo.vo;

import com.carpooling.common.pojo.db.User;
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
     * 状态 0 正常 1 未注册 2 未电话认证 3 未学生认证 4 都没有认证
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
