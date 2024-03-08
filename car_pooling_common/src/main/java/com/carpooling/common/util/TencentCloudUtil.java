package com.carpooling.common.util;

import com.carpooling.common.properties.TencentCloudProperties;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author LiangHanSggg
 * @date 2024-03-05 21:24
 */
@Component
public class TencentCloudUtil {


    @Autowired
    private TencentCloudProperties tencentCloudProperties;


    /**
     * 指定手机号发送短信验证码
     *
     * @param phone 手机号
     * @param code  验证码
     */
    public void sendPhoneCode(String phone, String code) {
        // 认证对象,提供短信服务账号的id和密钥
        Credential cred = new Credential(tencentCloudProperties.getSecretId(), tencentCloudProperties.getSecretKey());
        // 实例化客户端对象
        SmsClient smsClient = new SmsClient(cred, "ap-guangzhou");
        // 构建请求对象
        SendSmsRequest request = new SendSmsRequest();
        // 设置应用id
        request.setSmsSdkAppId(tencentCloudProperties.getSdkAppId());
        // 设置签名
        request.setSignName(tencentCloudProperties.getSignName());
        // 设置模板id
        request.setTemplateId(tencentCloudProperties.getCodeTemplateId());
        // 指定手机号
        request.setPhoneNumberSet(new String[]{phone});
        // 设置验证码
        request.setTemplateParamSet(new String[]{code});
        try {
            // 发送请求
            smsClient.SendSms(request);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException(e);
        }
    }
}
