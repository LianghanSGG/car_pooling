package com.carpooling.common.interceptor;

import com.carpooling.common.pojo.vo.UserVO;
import com.carpooling.common.util.HttpUtil;
import com.carpooling.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author LiangHanSggg
 * @date 2023-08-06 19:16
 */
@Slf4j
@Component
public class MockInter extends HandlerInterceptorAdapter {

    @Autowired
    HttpUtil hp;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String id = request.getParameter("id");


//        UserVO LiangHan = new UserVO();
//        LiangHan.setId(1691353490296762370L);
//        LiangHan.setState(0);
//        LiangHan.setClientIP(hp.getClientIP(request));
//        LiangHan.setOpenid("om0Ab5GmA9eMzEbNhBmmUtPkHRP4");
//        UserContext.set(LiangHan);

        UserVO cy = new UserVO();
        cy.setId(1774401246095523842L);
        cy.setState(0);
        cy.setClientIP(hp.getClientIP(request));
        cy.setOpenid("om0Ab5FhV0_UfY6cXftkbnkHgwkY");
        UserContext.set(cy);

//        UserVO YangSenJie = new UserVO();
//        YangSenJie.setId(1766004942109773826L);
//        YangSenJie.setState(0);
//        YangSenJie.setClientIP(hp.getClientIP(request));
//        YangSenJie.setOpenid("om0Ab5CjwxHSNCzyQQaGW5r33f30");
//        UserContext.set(YangSenJie);

//        UserVO GUOPEIXIONG = new UserVO();
//        GUOPEIXIONG.setId(1765247479326564354L);
//        GUOPEIXIONG.setState(0);
//        GUOPEIXIONG.setClientIP(hp.getClientIP(request));
//        GUOPEIXIONG.setOpenid("om0Ab5JfwWmoGstn3-zUE3mQl-Ro");
//        UserContext.set(GUOPEIXIONG);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove();
        return;
    }
}
