package com.carpooling.common.util;

import com.carpooling.common.pojo.vo.UserVO;

/**
 * @author LiangHanSggg
 * @date 2023-07-16 19:21
 */
public class UserContext {

    private static final ThreadLocal<UserVO> user_Context = ThreadLocal.withInitial(UserVO::new);


    public static void set(UserVO user) {
        if (user != null) {
            user_Context.set(user);
        }
    }

    public static UserVO get() {
        return user_Context.get();
    }

    public static void remove() {
        user_Context.remove();
    }


}
