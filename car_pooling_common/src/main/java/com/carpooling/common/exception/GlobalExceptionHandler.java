package com.carpooling.common.exception;

import com.carpooling.common.pojo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

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

    @ExceptionHandler(PreCheckException.class)
    public R<String> preCheckException(Exception e) {
        return R.fail(e.getMessage());
    }

    @ExceptionHandler(serviceLogic.class)
    public R<String> serviceLogicException(Exception e) {
        return R.fail(e.getMessage());
    }

    @ExceptionHandler(OrderVerifyException.class)
    public R<String> orderVerifyException(Exception e) {
        return R.fail(e.getMessage());
    }

    // post 并且使用@Valid@RequestBody接受参数就会出错
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        StringBuilder errorMsg = new StringBuilder();
        for (ObjectError error : allErrors) {
            errorMsg.append(error.getDefaultMessage()).append("; ");
        }
        return R.fail(errorMsg.toString());
    }

    // 加了Request但是没有传递参数
    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public R<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return R.fail("缺少参数" + ex.getParameterName());
    }

//    // 参数写在类中， get方法出错会被捕获
//    @ExceptionHandler(value = {BindException.class})
//    public R<String> handleBindException(BindException ex) {
//        return R.error(ex.getBindingResult().getFieldError().getDefaultMessage());
//    }


    /**
     * 用于捕获@RequestParam/@PathVariable参数触发校验规则抛出的异常
     * 如果没加request但是有参数限制的
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public R<String> handleConstraintViolationException(ConstraintViolationException e) {
        StringBuilder sb = new StringBuilder();
        Set<ConstraintViolation<?>> conSet = e.getConstraintViolations();
        for (ConstraintViolation<?> con : conSet) {
            String message = con.getMessage();
            sb.append(message).append(";");
        }
        return R.fail(sb.toString());
    }


}
