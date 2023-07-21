package com.carpooling.common.config;

import com.carpooling.common.properties.AsyncConstants;
import com.carpooling.common.properties.OrdinaryAsyncConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * SpringBoot线程池配置
 *
 * @author LiangHanSggg
 * @date 2023-07-18 19:14
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
public class TheadPoolConfig{


    private final OrdinaryAsyncConstants ordinaryAsyncConstants;

    @Lazy
    @Autowired
    public TheadPoolConfig(OrdinaryAsyncConstants ordinaryAsyncConstants) {
        this.ordinaryAsyncConstants = ordinaryAsyncConstants;
    }


    @Bean(name = "ordinaryThreadPool")
    public ThreadPoolTaskExecutor getOrdinaryThreadPool() {
        return init(ordinaryAsyncConstants);
    }


    private ThreadPoolTaskExecutor init(AsyncConstants asyncConstants) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 初始化
        executor.initialize();
        executor.setCorePoolSize(asyncConstants.getCorePoolSize());
        executor.setMaxPoolSize(asyncConstants.getMaxPoolSize());
        executor.setQueueCapacity(asyncConstants.getQueueCapacity());
        executor.setKeepAliveSeconds(asyncConstants.getKeepAliveSeconds());
        executor.setThreadNamePrefix(asyncConstants.getPrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

}
