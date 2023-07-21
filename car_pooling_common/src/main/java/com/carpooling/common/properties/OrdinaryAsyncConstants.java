package com.carpooling.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * IO密集型配置
 *
 * @author LiangHanSggg
 * @date 2023-07-18 19:11
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ordinary.async")
public class OrdinaryAsyncConstants extends AsyncConstants {
}
