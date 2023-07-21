package com.carpooling.common.util;

import cn.hutool.http.HttpUtil;
import com.carpooling.common.pojo.WxLoginEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 微信工具
 *
 * @author LiangHanSggg
 * @date 2023-07-20 16:10
 */
@Slf4j
@Component
public class WxUtil {

    @Value("${wx.config.appid}")
    private String appId;


    @Value("${wx.config.secret}")
    private String secret;

    private static final String url = "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={2}&grant_type=authorization_code";

    @Autowired
    ObjectMapper objectMapper;


    public WxLoginEntity wxLogin(String code) throws JsonProcessingException {
        String replace = url.replace("{0}", appId).replace("{1}", secret).replace("{2}", code);

        String json = HttpUtil.get(replace);

        return objectMapper.readValue(json, WxLoginEntity.class);

    }


}
