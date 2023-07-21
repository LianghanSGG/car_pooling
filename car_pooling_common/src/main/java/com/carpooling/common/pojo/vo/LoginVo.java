package com.carpooling.common.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangHanSggg
 * @date 2023-07-20 16:56
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginVo {

    /**
     * 用户的状态： 0正常用户， 1 都没有认证 ，2未进行电话号认证，3未进行学生认证, 4 刚注册。
     */
    Integer state;

    /**
     * 昵称
     */
    String nickName;

    /**
     * 电话
     */
    String phone;

    /**
     * token
     */
    String token;


}
