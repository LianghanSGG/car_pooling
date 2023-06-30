package com.carpooling.common.exception;

import com.carpooling.common.pojo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author LiangHanSggg
 * @date 2023-06-30 18:20
 */
@RestControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(DException.class)
    public R<String> diyExceptionHandler(Exception e) {
        log.warn("出现自定义异常：{}", e.getMessage());
        return R.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<String> ExceptionHandler(Exception e) {
        log.warn("出现异常：{}", e.getMessage());
        return R.error("系统出错");
    }



}
