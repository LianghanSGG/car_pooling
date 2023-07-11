package com.carpooling.monitor.service.Event;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.carpooling.monitor.service.observer.AbsInsertObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 插入事件，通知观察者
 *
 * @author LiangHanSggg
 * @date 2023-07-11 19:37
 */
@Component
public class InsertEventSubject extends AbsEventSubject {

    private int size;

    final List<AbsInsertObserver> insertObserverList;

    @Autowired
    public InsertEventSubject(List<AbsInsertObserver> list) {
        this.insertObserverList = list;
        this.size = list.size();
    }

    @Override
    public void notifyList(CanalEntry.RowData rowData) throws InterruptedException {
        if (size == 0) {
            return;
        }

        for (int i = 0; i < size; i++) {
            insertObserverList.get(i).process(rowData);
        }

    }
}
