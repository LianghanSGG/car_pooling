package com.carpooling.common.interceptor;

import com.carpooling.common.pojo.vo.UserVO;
import com.carpooling.common.util.HttpUtil;
import com.carpooling.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author LiangHanSggg
 * @date 2023-08-14 14:50
 */
@Component
public class IPInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    HttpUtil hp;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserVO userVO1 = new UserVO();
        userVO1.setClientIP(hp.getClientIP(request));
        UserContext.set(userVO1);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove();
    }
}
