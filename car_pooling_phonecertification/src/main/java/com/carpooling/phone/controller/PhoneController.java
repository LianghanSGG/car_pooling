package com.carpooling.phone.controller;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import com.carpooling.common.annotation.Log;
import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.vo.PhoneCodeVerifyVO;
import com.carpooling.common.service.BlackListService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import com.carpooling.phone.service.PhoneVerifySuccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

/**
 * @author LiangHanSggg
 * @date 2023-08-03 20:10
 */
@Validated
@RestController
@RequestMapping("/test/phone")
public class PhoneController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    BlackListService blackListService;

    @Autowired
    PhoneVerifySuccessService phoneVerifySuccessService;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 发送验证码,有效期15分钟
     *
     * @param phoneCodeVerifyVO
     * @return
     */
    @Log(module = "电话验证模块", operation = "发送验证码")
    @GetMapping("/send")
    public R getMessage(@RequestBody PhoneCodeVerifyVO phoneCodeVerifyVO) {

        Long id = UserContext.get().getId();
        if (blackListService.checkExist(id)) {
            return R.success("已经进入黑名单，有问题反馈");
        }

        return R.success(phoneVerifySuccessService.send(phoneCodeVerifyVO.getPhone()));
    }

    // TODO: 2023/8/5 进行测试！关于校验的
    /**
     * 校验验证码
     *
     * @param phoneCodeVerifyVO
     * @return
     */
    @Log(module = "电话验证模块", operation = "验证码校验")
    @PostMapping("/verify")
    public R verifyCode(@Valid @RequestBody PhoneCodeVerifyVO phoneCodeVerifyVO) {
        if (Objects.isNull(phoneCodeVerifyVO.getCode())) return R.fail("验证码不能为空");
        BitMapBloomFilter filter = new BitMapBloomFilter(10);

        Long id = UserContext.get().getId();
        if (blackListService.checkExist(id)) {
            return R.success("已经进入黑名单，有问题反馈");
        }

        return R.success(phoneVerifySuccessService.verify(phoneCodeVerifyVO));
    }


}
