package com.carpooling.common.config;

import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 图片配置
 * @author LiangHanSggg
 * @date 2023-07-30 19:15
 */
@Component
public class PicConfig {
    @Autowired
    private com.carpooling.common.properties.QnyPicConstants QnyPicConstants;

    @Bean
    public Configuration qiniuConfig() {
        return new Configuration(Region.autoRegion());
    }

    @Bean
    public Auth auth() {
        return Auth.create(QnyPicConstants.getAccessKey(), QnyPicConstants.getSecretKey());
    }

    @Bean
    public BucketManager bucketManager(Auth auth, Configuration configuration) {
        return new BucketManager(auth, configuration);
    }
}
