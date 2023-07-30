package com.carpooling.monitor.service.observer;

import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.monitor.service.Event.QuestionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Questions表的更新、删除事件，应该将Redis中的缓存删掉
 *
 * @author LiangHanSggg
 * @date 2023-07-27 20:33
 */
@Component
public class QuestionUpListener implements ApplicationListener<QuestionEvent> {


    @Autowired
    RedisUtil redisUtil;


    @Override
    public void onApplicationEvent(QuestionEvent event) {
        redisUtil.AsyncDeleted(RedisPrefix.QA);
        return;
    }
}
