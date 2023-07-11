package com.carpooling.monitor.service.Event;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.carpooling.monitor.service.observer.AbsUpdateObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author LiangHanSggg
 * @date 2023-07-11 19:39
 */
@Component
public  class updateEventSubject extends AbsEventSubject {
    private int size;
    final List<AbsUpdateObserver> updateObserverList;

    @Autowired
    public updateEventSubject(List<AbsUpdateObserver> list) {
        this.updateObserverList = list;
        this.size = list.size();
    }

    /**
     * @param rowData 发生变化的数据库数据对象
     * @throws InterruptedException 中断异常
     */
    @Override
    public void notifyList(CanalEntry.RowData rowData) throws InterruptedException {
        if (size == 0) {
            return;
        }

        for (int i = 0; i < size; i++) {
            updateObserverList.get(i).process(rowData);
        }

    }
}
