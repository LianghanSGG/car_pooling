package com.carpooling.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取yml文件中有关腾讯云的配置
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "tencent-cloud.sms")
public class TencentCloudProperties {


    private String secretId;

    private String secretKey;

    private String sdkAppId;

    private String signName;

    private String codeTemplateId;



}
