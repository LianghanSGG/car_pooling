package com.carpooling.monitor.service.observer;

import com.alibaba.otter.canal.protocol.CanalEntry;

/**
 * @author LiangHanSggg
 * @date 2023-07-11 19:41
 */
public abstract class AbsObserver {
    public abstract void beforeProcess();

    public abstract void afterProcess();

    public abstract void handler(CanalEntry.RowData rowData);

    // 暂时不需要CountDownLatch
//    public void process(CanalEntry.RowData rowData, CountDownLatch latch) {
//        beforeProcess();
//
//        handler(rowData);
//
//        afterProcess();
//
//        latch.countDown();
//    }

    public void process(CanalEntry.RowData rowData) {
        beforeProcess();

        handler(rowData);

        afterProcess();

    }


}
