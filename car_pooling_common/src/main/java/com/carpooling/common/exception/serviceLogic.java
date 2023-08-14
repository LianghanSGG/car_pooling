package com.carpooling.common.exception;

/**
 * 业务逻辑异常，是用于一些没有通过限制条件的请求
 *
 * @author LiangHanSggg
 * @date 2023-08-07 21:37
 */
public class serviceLogic extends RuntimeException {
    public serviceLogic(String message) {
        super(message);
    }
}
