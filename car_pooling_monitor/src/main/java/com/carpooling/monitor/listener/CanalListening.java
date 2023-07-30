package com.carpooling.monitor.listener;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.carpooling.monitor.service.Event.QuestionEvent;
import com.carpooling.monitor.service.Event.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

/**
 * canal监听中心
 * 主要实现事件的创建和推送
 * 表的个数是有限的，如果使用观察者模式，以现在需求很容易出现类爆炸，因此使用Spring的监听事务机制。
 * 事件是表，具体是表的增事件或是改事件，通过事件里面的标识位进行区分。
 * 我们要监听之后实现一些功能只需要创建一个具体的监听器监听我们要的事件，然后要对事件的类型进行判断
 * 后期同一个事件的监听器多了的话可以并发执行
 * 注意每一个监听器应该尽量的被try catch捕获，出现异常容易导致后序的事件不执行
 *
 * @author LiangHanSggg
 * @date 2023-07-26 19:29
 */
@Slf4j
@Component
public class CanalListening implements ApplicationRunner {

    @Value("${canal.hostname}")
    private String hostname;
    @Value("${canal.port}")
    private Integer port;
    @Value("${canal.destination}")
    private String destination;
    @Value("${canal.batchSize}")
    private Integer bathSize;

    @Autowired
    private ApplicationContext applicationContext;



    public void startListening() {
        long batchId = 0;
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(hostname, port), destination, "", "");
        System.out.println("开始连接");
        try {
            connector.connect();
            //这里直接使用配置文件里面
            connector.subscribe();
            connector.rollback();
            while (true) {
                Message message = connector.getWithoutAck(bathSize);
                batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    try {
                        Thread.currentThread().sleep(2000);
                    } catch (InterruptedException e) {
                        log.info("线程出现异常");
                    }
                } else {
                    distribute(message.getEntries());
                }
                connector.ack(batchId);
            }

        } catch (Exception e) {
            if (batchId != 0) {
                connector.rollback(batchId);
            }

        } finally {
            connector.disconnect();
        }
    }

    private void distribute(List<CanalEntry.Entry> entries) {

        for (CanalEntry.Entry entry : entries) {

            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                log.info("entry的反序列化出现异常{}", entry);
            }

            //获得表名
            String tableName = entry.getHeader().getTableName();
            //获得操作类型
            CanalEntry.EventType eventType = rowChage.getEventType();

            System.out.println("表名:---------------" + tableName);

            if (rowChage.getIsDdl()) {
                log.info("出现DDL语句：{}", rowChage.getSql());
                continue;
            }

            ApplicationEvent event = getEvent(tableName, eventType, entry);
            if (Objects.isNull(event)) continue;

            applicationContext.publishEvent(event);

        }


    }

    private ApplicationEvent getEvent(String tableName, CanalEntry.EventType eventType, CanalEntry.Entry entry) {
        int flag = 2;
        if (eventType == CanalEntry.EventType.INSERT) {
            flag = 0;
        } else if (eventType == CanalEntry.EventType.UPDATE) {
            flag = 1;
        } else {
            return null;
        }
        //后期填上去
        if ("questions".equals(tableName)) {
            return new QuestionEvent(this, flag, entry);
        } else if ("user".equals(tableName)) {
            return new UserEvent(this, flag, entry);
        } else {
            return null;
        }
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        startListening();
    }
}
