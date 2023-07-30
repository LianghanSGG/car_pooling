package com.carpooling.start.controller;


import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.TestEntity;
import com.carpooling.common.pojo.vo.StuCertVO;
import com.carpooling.common.pojo.vo.UserVO;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.service.UserService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import com.carpooling.start.service.ThService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 项目启动连接测试类
 *
 * @author LiangHanSggg
 * @date 2023-06-30 20:16
 */
@Validated
@RestController
@Slf4j
public class testController {

    @Autowired
    ThService thService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserService userService;

    /**
     * 连接测试1
     *
     * @param id
     * @return
     */
    @GetMapping("/testLinking")
    public R<TestEntity> test(@RequestParam("id") String id) {

        TestEntity testEntity = new TestEntity();
        testEntity.setId(10101010101L);
        testEntity.setCreateTime(LocalDateTime.now());
        testEntity.setUpdateTime(LocalDateTime.now());
        System.out.println(UserContext.get());

        return R.success(testEntity);
    }

    /**
     * 连接测试2
     *
     * @return
     */
    @GetMapping("/test")
    public R<TestEntity> test2() {

        redisUtil.AsyncDeleted("1");
        System.out.println("主线程开始睡眠");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return R.success(null);
    }

    @GetMapping("/test/user/putest")
    public R<TestEntity> test3() {

        UserVO userVO = new UserVO();
        userVO.setOpenid("1111111111");
        userVO.setId(1681962715833606145L);

        redisTemplate.opsForValue().set(RedisPrefix.USER + "wciI6lmIKZ4WCBSFWu7KDBMffwC_REEN2HAApT9bFPI", userVO);

        return R.success(null);
    }

    @GetMapping("/test/nopara")
    public R<String> get(String id) {
        System.out.println(id);
        System.out.println("1111111111111111");
        return R.success("成功");
    }

    /**
     * 测试连接3
     *
     * @param stuCertVO
     * @return
     */
    @PostMapping("/test/stuCert")
    public R<StuCertVO> poTest(@RequestBody StuCertVO stuCertVO) {
        return R.success(stuCertVO);
    }
}
