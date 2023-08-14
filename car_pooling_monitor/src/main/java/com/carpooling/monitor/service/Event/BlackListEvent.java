package com.carpooling.monitor.service.Event;

import com.alibaba.otter.canal.protocol.CanalEntry;
import org.springframework.context.ApplicationEvent;

/**
 * @author LiangHanSggg
 * @date 2023-07-30 17:37
 */
public class BlackListEvent extends ApplicationEvent {

    //0 是插入事件 1是更新事件
    private int flag;

    private CanalEntry.RowChange rowChange;


    public BlackListEvent(Object source, int i, CanalEntry.RowChange rowChange) {
        super(source);
        this.flag = i;
        this.rowChange = rowChange;
    }

    public int getFlag() {
        return flag;
    }

    public CanalEntry.RowChange getEntry() {
        return rowChange;
    }

}
