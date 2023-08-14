package com.carpooling.common.exception;

/**
 * 订单校验异常
 *
 * @author LiangHanSggg
 * @date 2023-08-08 14:13
 */
public class OrderVerifyException extends RuntimeException {
    public OrderVerifyException(String message) {
        super(message);
    }
}
