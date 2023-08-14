package com.carpooling.common.exception;

/**
 * 前置资格校验异常
 * @author LiangHanSggg
 * @date 2023-08-06 19:13
 */
public class PreCheckException extends RuntimeException{
    public PreCheckException(String message){super(message);}
}
