package com.carpooling.monitor.service.observer;

import com.carpooling.monitor.service.Event.UserEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 监听User表的更新任务，进行...逻辑
 *
 * @author LiangHanSggg
 * @date 2023-07-27 20:35
 */
@Component
public class userUpListener implements ApplicationListener<UserEvent> {
    @Override
    public void onApplicationEvent(UserEvent event) {
        if (event.getFlag() == 0) return;
        System.out.println("进入userUpListener事件，事件的类型是:" + event.getFlag());
        return;
    }
}
