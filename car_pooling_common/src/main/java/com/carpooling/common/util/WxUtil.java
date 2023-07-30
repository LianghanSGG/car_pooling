package com.carpooling.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.carpooling.common.pojo.WxLoginEntity;
import com.carpooling.common.prefix.RedisPrefix;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private RedisUtil redisUtil;

    // 登录请求
    private static final String Login_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={2}&grant_type=authorization_code";

    // 权限Token
    // 等获得了之后可以直接写死
    private static final String AccessToken_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={0}&secret={1}";

    // 消息推送
    private static final String senMessage_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token={0}";

    /**
     * 微信登录
     * @param code 前端传来的code
     * @return
     * @throws JsonProcessingException
     */
    public WxLoginEntity wxLogin(String code) throws JsonProcessingException {
        String replace = Login_URL.replace("{0}", appId).replace("{1}", secret).replace("{2}", code);

        String json = HttpUtil.get(replace);

        return objectMapper.readValue(json, WxLoginEntity.class);

    }


    public String getAccessToken() {

        String s = redisUtil.StringGet(RedisPrefix.ACCESS_TOKEN, String.class);

        if (StrUtil.isEmptyIfStr(s)) {
            JSONObject jsonObject = JSONUtil.parseObj(HttpUtil.get(AccessToken_URL.replace("{0}", appId).replace("{1}", secret)));
            String access_token = jsonObject.getStr("access_token");
            if (!StrUtil.isEmptyIfStr(access_token)) {
                Integer expires_in = jsonObject.getInt("expires_in");
                redisUtil.StringAdd(RedisPrefix.ACCESS_TOKEN, access_token, expires_in.intValue(), TimeUnit.SECONDS);
                return access_token;
            }


            /**
             * -1	系统繁忙，此时请开发者稍候再试
             * 40001	i获取 access_token 时 AppSecret 错误，或者 access_token 无效。请开发者认真比对 AppSecret 的正确性，或查看是否正在为恰当的公众号调用接口
             * 40013	不合法的 AppID ，请开发者检查 AppID 的正确性，避免异常字符，注意大小写
             */
            int count = 0;
            boolean b = false;
            do {
                jsonObject = JSONUtil.parseObj(HttpUtil.get(AccessToken_URL.replace("{0}", appId).replace("{1}", secret)));
                access_token = jsonObject.getStr("access_token");

                count++;

            } while ((b = StrUtil.isEmptyIfStr(access_token)) && count < 2);

            if (!b) {
                Integer expires_in = jsonObject.getInt("expires_in");
                redisUtil.StringAdd(RedisPrefix.ACCESS_TOKEN, access_token, expires_in.intValue(), TimeUnit.SECONDS);
                return access_token;
            } else {
                throw new RuntimeException("获得access_token出现异常");
            }
        } else {
            return s;
        }


    }


    /**
     * 在任何调用了包含这个方法的controller得检查openid是否存在
     *
     * @param template 模板消息的id
     * @param json     传入的数据，应该按照模板id的格式进行修改;
     */
    public void sendMessage(String template, JSONObject json) {
        String openid = UserContext.get().getOpenid();
        JSONObject body = new JSONObject();
        body.set("touser", openid);
        body.set("template_id", template);
        body.set("data", json);
        HttpUtil.post(senMessage_URL.replace("{0}", getAccessToken()), body.toString());
    }

}
