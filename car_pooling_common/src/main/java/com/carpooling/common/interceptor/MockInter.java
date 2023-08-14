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
//
//        UserVO userVO = new UserVO();
//        userVO.setId(Long.parseLong(id));
//        userVO.setState(0);
//        userVO.setClientIP("127.0.01");
//        userVO.setOpenid("MOCK_OPENID");
//        UserContext.set(userVO);

        UserVO userVO = new UserVO();
        userVO.setId(1681962715833606148L);
        userVO.setState(0);
        userVO.setClientIP(hp.getClientIP(request));
        userVO.setOpenid("MOCK_OPENID");
        UserContext.set(userVO);
        return true;
    }
}
