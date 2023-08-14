package com.carpooling.start.controller;


import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.TestEntity;
import com.carpooling.common.pojo.db.Order;
import com.carpooling.common.pojo.vo.StuCertVO;
import com.carpooling.common.service.BlackListService;
import com.carpooling.common.service.OrderService;
import com.carpooling.common.service.UserService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目启动连接测试类
 *
 * @author LiangHanSggg
 * @date 2023-06-30 20:16
 */
@Validated
@RestController
@Slf4j
@RequestMapping("/mock")
public class testController {


    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserService userService;

    @Autowired
    BlackListService blackListService;

    @Autowired
    OrderService orderService;


    /**
     * 连接测试
     *
     * @param id
     * @return
     */
    @GetMapping("/test/Linking")
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
     * 测试连接数据库
     *
     * @return
     * @apiNote 测试连接数据库
     */
    @GetMapping("/test/LinkingDB")
    public R<List<Order>> test2() {
        return R.success(orderService.list());
    }

    /**
     * 测试连接3
     *
     * @return
     * @apiNote 测试连接redis id=1一直是常驻redis的
     */
    @GetMapping("/test/user/putest")
    public R test3(@RequestParam("id") String id) {
        return R.success(blackListService.checkExist(Long.parseLong(id)));
    }

    /**
     * 测试连接4
     *
     * @param stuCertVO
     * @return
     * @apiNote 发过来什么就回去什么
     */
    @PostMapping("/test/stuCert")
    public R<StuCertVO> poTest(@RequestBody StuCertVO stuCertVO) {
        return R.success(stuCertVO);
    }
}
