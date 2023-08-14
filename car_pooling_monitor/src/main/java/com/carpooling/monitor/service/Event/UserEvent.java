package com.carpooling.monitor.service.Event;

import com.alibaba.otter.canal.protocol.CanalEntry;
import org.springframework.context.ApplicationEvent;

/**
 * User表事件
 *
 * @author LiangHanSggg
 * @date 2023-07-27 20:36
 */
public class UserEvent extends ApplicationEvent {

    //0 是插入事件 1是更新事件
    private int flag;

    private CanalEntry.RowChange rowChage;

    public UserEvent(Object source,int i, CanalEntry.RowChange rowChage) {
        super(source);
        this.flag = i;
        this.rowChage = rowChage;
    }

    public int getFlag() {
        return flag;
    }

    public CanalEntry.RowChange getEntry() {
        return rowChage;
    }
}
