package com.carpooling.common.util;


import com.carpooling.common.pojo.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.util.annotation.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author LiangHanSggg
 * @date 2023-07-17 14:42
 */
@Component
public class HttpUtil {

    @Autowired
    ObjectMapper objectMapper;

    /**
     * 成功的时候自行决定是否要加入数据
     *
     * @param response
     * @param success  是否成功
     * @param code
     * @param message
     * @param data
     * @throws IOException
     */
    public void response(HttpServletResponse response, boolean success, @Nullable Integer code, @Nullable String message, @Nullable Object data) throws IOException {
        response.setStatus(code);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");

        if (success) {
            if (code != null) {
                response.getWriter().write(objectMapper.writeValueAsString(R.success(data, code)));
            } else {
                response.getWriter().write(objectMapper.writeValueAsString(R.success(data)));

            }
        } else {
            if (code != null) {
                response.getWriter().write(objectMapper.writeValueAsString(R.error(message, code)));
            } else {
                response.getWriter().write(objectMapper.writeValueAsString(R.error(message)));

            }
        }
    }

    public String getClientIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
