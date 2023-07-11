package com.carpooling.monitor.service.Event;

import com.alibaba.otter.canal.protocol.CanalEntry;

/**
 * 抽象事件
 * @author LiangHanSggg
 * @date 2023-07-11 19:35
 */
public abstract class AbsEventSubject {
    public abstract void notifyList(CanalEntry.RowData rowData) throws InterruptedException;

}
