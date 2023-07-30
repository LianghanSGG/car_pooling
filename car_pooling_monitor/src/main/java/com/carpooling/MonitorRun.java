package com.carpooling;

import com.carpooling.monitor.listener.CanalListening;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author LiangHanSggg
 * @date 2023-07-26 17:46
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class MonitorRun{

    @Autowired
    CanalListening canalListening;

    public static void main(String[] args) {
        SpringApplication.run(MonitorRun.class, args);
    }


}
