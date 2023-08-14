package com.carpooling.common.annotation;


import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.vo.UserVO;
import com.carpooling.common.util.UserContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;


/**
 * 日志切面实现
 * 第二版可以切换为异步日志，但是需要先拿到参数然后log。不要在log中去获得TheadLocal中的值很有可能出错
 */
@Aspect
@Component
@Slf4j
public class LogAspect {


    @Autowired
    private ObjectMapper m1;


    //表示匹配带有自定义注解的方法
    @Pointcut("@annotation(com.carpooling.common.annotation.Log)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        //获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        Log userAction = method.getAnnotation(Log.class);

        Object result = null;
        result = point.proceed();


        UserVO userVO = UserContext.get();


        log.info("UserIP:{}，Userid:{}，Module:{}，Operation:{}，Result:{}，Request_type:{}，Class:{}，Method:{}，Parameter:{}, Reponse:{}"
                , userVO != null ? userVO.getClientIP() : "未知IP", userVO != null ? userVO.getId() : "未知ID", userAction.module(), userAction.operation(), "成功", request.getMethod()
                , point.getTarget().getClass().getName(), signature.getName(), m1.writeValueAsString(getParameter(method, point.getArgs())), m1.writeValueAsString(result));
        return result;
    }

    /**
     * 配置异常通知
     */
    @AfterThrowing(pointcut = "pointcut()", throwing = "e")
    public R<String> afterThrowing(JoinPoint point, Throwable e) throws JsonProcessingException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        Log userAction = method.getAnnotation(Log.class);

        UserVO userVO = UserContext.get();

        log.info("UserIP:{}，Userid:{}，Module:{}，Operation:{}，Result:失败->{}，Request_type:{}，Class:{}，Method:{}，Parameter:{}"
                , userVO != null ? userVO.getClientIP() : "未知IP", userVO != null ? userVO.getId() : "未知ID", userAction.module(), userAction.operation(), e.getMessage(), request.getMethod()
                , point.getTarget().getClass().getName(), signature.getName(), m1.writeValueAsString(getParameter(method, point.getArgs())));
        return R.error(e.getMessage());
    }

    /**
     * 根据方法和传入的参数获取请求参数
     */
    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            //将RequestBody注解修饰的参数作为请求参数
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            //将RequestParam注解修饰的参数作为请求参数
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if (StringUtils.hasText(requestParam.value())) {
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if (argList.size() == 0) {
            return null;
        } else if (argList.size() == 1) {
            return argList.get(0);
        } else {
            return argList;
        }
    }

}
