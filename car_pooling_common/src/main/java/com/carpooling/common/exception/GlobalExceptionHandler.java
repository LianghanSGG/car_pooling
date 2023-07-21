package com.carpooling.common.exception;

import com.carpooling.common.pojo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

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
        //暂时将异常给前端看
//        return R.error("稍后重试");
        return R.error(e.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        StringBuilder errorMsg = new StringBuilder();
        for (ObjectError error : allErrors) {
            errorMsg.append(error.getDefaultMessage()).append("; ");
        }
        return R.data_error(errorMsg.toString());
    }


}
