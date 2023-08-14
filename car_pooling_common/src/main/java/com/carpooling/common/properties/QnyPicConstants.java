package com.carpooling.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 七牛云OSS配置常量
 *
 * @author LiangHanSggg
 * @date 2023-07-30 19:18
 */
@Component
@ConfigurationProperties(prefix = "file.qiniuy")
@Data
public class QnyPicConstants {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String url;
    private int expireSeconds;
}
