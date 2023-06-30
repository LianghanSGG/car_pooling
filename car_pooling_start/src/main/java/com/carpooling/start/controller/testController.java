package com.carpooling.start.controller;


import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 项目启动连接测试类
 *
 * @author LiangHanSggg
 * @date 2023-06-30 20:16
 */
@RestController
@Slf4j
public class testController {

    @GetMapping("/testLinking")
    public R<TestEntity> test(@RequestParam("id") String id) {
        log.info("接受到参数:{}", id);
        TestEntity testEntity = new TestEntity();
        testEntity.setId(10101010101L);
        testEntity.setCreateTime(LocalDateTime.now());
        testEntity.setUpdateTime(LocalDateTime.now());
        return R.success(testEntity);
    }

}
