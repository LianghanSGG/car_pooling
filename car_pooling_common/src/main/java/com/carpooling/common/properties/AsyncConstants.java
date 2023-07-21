package com.carpooling.common.properties;

import lombok.Data;

/**
 * 多线程配置常量
 *
 * @author LiangHanSggg
 * @date 2023-07-18 19:10
 */
@Data
public class AsyncConstants {
    int corePoolSize;
    int maxPoolSize;
    int keepAliveSeconds;
    int queueCapacity;
    String prefix;
}
