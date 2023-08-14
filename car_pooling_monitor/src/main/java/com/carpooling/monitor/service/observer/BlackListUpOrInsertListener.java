package com.carpooling.monitor.service.observer;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.monitor.service.Event.BlackListEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 更新事件只有删除，如果是插入事件那就得加入redis的集合中
 * 注意后台管理系统不要使用批处理
 *
 * @author LiangHanSggg
 * @date 2023-07-30 17:36
 */
@Slf4j
@Component
public class BlackListUpOrInsertListener implements ApplicationListener<BlackListEvent> {

    @Autowired
    RedisUtil redisUtil;


    @Override
    public void onApplicationEvent(BlackListEvent event) {
        try {
            int flag = event.getFlag();
            CanalEntry.RowChange rowChange = event.getEntry();
            if (flag == 0) {
                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
                    for (CanalEntry.Column column : columns) {
                        if ("user_id".equals(column.getName())) {
                            redisUtil.SetAddNoExpire(RedisPrefix.BLACKLIST, column.getValue());
                            log.info("BlackListUpOrInsertListener新增事件,增加user_id:{}", column.getValue());
                        }
                    }
                }

            } else {

                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
                    String userId = null;

                    for (CanalEntry.Column column : afterColumnsList) {
                        if ("user_id".equals(column.getName())) {
                            userId = column.getValue();
                        }
                        if ("deleted".equals(column.getName()) && "1".equals(column.getValue())) {
                            redisUtil.SetDeleted(RedisPrefix.BLACKLIST, userId);
                            log.info("BlackListUpOrInsertListener更新事件,删除的user_id:{}", userId);
                        }
                    }

                }

            }
        } catch (Exception e) {
            log.error("BlackListUpOrInsertListener出现异常{}", e.getMessage());
        }

    }

    private static void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}
