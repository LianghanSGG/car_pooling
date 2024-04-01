package com.carpooling.common.pojo;

import lombok.Data;

/**
 * 通用结果返回类
 */
@Data
public class R<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 状态信息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    public static <T> R<T> success(T object) {
        R<T> result = new R<T>();
        result.data = object;
        result.code = 200;
        result.msg = "成功";
        return result;
    }

    public static <T> R<T> success(T object, Integer code) {
        R<T> result = new R<T>();
        result.data = object;
        result.code = code;
        result.msg = "成功";
        return result;
    }

    public static <T> R<T> success() {
        R<T> result = new R<T>();
        result.code = 200;
        result.msg = "成功";
        return result;
    }

    public static <T> R<T> error(String msg) {
        R result = new R();
        result.msg = msg;
        result.code = 400;
        return result;
    }

    public static <T> R<T> error(String msg, Integer code) {
        R result = new R();
        result.msg = msg;
        result.code = code;
        return result;
    }

    public static <T> R<T> fail(String msg) {
        R result = new R();
        result.msg = msg;
        result.code = 400;
        return result;
    }

    public static <T> R<T> data_error(String msg) {
        R result = new R();
        result.msg = msg;
        result.code = 401;
        return result;
    }
}
