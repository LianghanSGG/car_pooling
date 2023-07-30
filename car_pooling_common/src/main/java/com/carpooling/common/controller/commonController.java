package com.carpooling.common.controller;

import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * @author LiangHanSggg
 * @date 2023-06-30 20:54
 */
@RestController
@Slf4j
public class commonController {

    /**
     * 特殊模块
     * @return
     */
    @GetMapping("/testLinkingCommon")
    public R<TestEntity> test2() {
        TestEntity testEntity = new TestEntity();
        testEntity.setId(10101010101L);
        testEntity.setCreateTime(LocalDateTime.now());
        testEntity.setUpdateTime(LocalDateTime.now());
        return R.success(testEntity);
    }
}
