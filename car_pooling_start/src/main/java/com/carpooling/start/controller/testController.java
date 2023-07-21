package com.carpooling.start.controller;


import cn.hutool.core.util.RandomUtil;
import com.carpooling.common.annotation.Log;
import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.TestEntity;


import com.carpooling.common.pojo.db.User;
import com.carpooling.common.pojo.vo.UserVO;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.service.UserService;
import com.carpooling.common.util.JwtUtil;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import com.carpooling.start.service.ThService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 项目启动连接测试类
 *
 * @author LiangHanSggg
 * @date 2023-06-30 20:16
 */
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

    @Log(module = "测试模块")
    @GetMapping("/testLinking")
    public R<TestEntity> test(@RequestParam("id") String id) {

        TestEntity testEntity = new TestEntity();
        testEntity.setId(10101010101L);
        testEntity.setCreateTime(LocalDateTime.now());
        testEntity.setUpdateTime(LocalDateTime.now());
        System.out.println(UserContext.get());

        return R.success(testEntity);
    }

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

    @GetMapping("/user/putest")
    public R<TestEntity> test3() {

//        User user = new User();
//        user.setName("测试是否能拿到id");
//        userService.save(user);
//        System.out.println(user.getId());
        UserVO userVO = new UserVO();
        userVO.setOpenid("1111");
        userVO.setId(123L);

        redisTemplate.opsForValue().set(RedisPrefix.USER + "qjyzT4AUbwlAd-mpBUFbw0j-7Iy-3dol32c4h5eBGoU", userVO);

        return R.success(null);
    }

    public static void main(String[] args) throws InterruptedException {
//
        String token1 = JwtUtil.getToken("temp", 14L, 10000000);

        System.out.println(token1);


    }
}
