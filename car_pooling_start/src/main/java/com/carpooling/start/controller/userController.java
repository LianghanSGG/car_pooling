package com.carpooling.start.controller;

import cn.hutool.core.util.StrUtil;
import com.carpooling.common.annotation.Log;
import com.carpooling.common.exception.DException;
import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.vo.LoginVo;
import com.carpooling.common.pojo.vo.UserInfoVo;
import com.carpooling.common.service.UserService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * @author LiangHanSggg
 * @date 2023-07-20 16:29
 */
@Validated
@RestController
@RequestMapping("/user")
public class userController {

    @Autowired
    UserService userService;

    @Autowired
    RedisUtil redisUtil;

    /**
     * 用户登录接口
     *
     * @param code 从微信获得的code
     * @return
     */
    @Log(module = "用户模块", operation = "登录")
    @GetMapping("/login")
    public R<LoginVo> WxLogin(@NotBlank(message = "code不能是空字符串") @RequestParam("code") String code) {
        if (StrUtil.isEmptyIfStr(code)) {
            return R.fail("code不能为空");
        }
        return R.success(userService.wxLogin(code));
    }

    /**
     * 用户获得个人信息的接口
     *
     * @return
     */
    @Log(module = "用户模块", operation = "获得个人信息")
    @GetMapping("/info")
    public R<UserInfoVo> getUserInfo() {
        Long id = UserContext.get().getId();

        UserInfoVo info = userService.getInfo(id);
        if (Objects.isNull(info)) {
            throw new DException("个人信息为空");
        } else {
            return R.success(info);
        }
    }

    /**
     * 修改个人信息接口
     *
     * @param userInfoVo
     * @return
     * @apiNote 会判断是否符合时间要求
     */
    @Log(module = "用户模块", operation = "修改个人信息")
    @PostMapping("/info/update")
    public R updateInfo(@RequestBody @Valid UserInfoVo userInfoVo) {
        Long id = UserContext.get().getId();
//        String s = redisUtil.StringGet(RedisPrefix.USERINFO_TIME + id, String.class);
//        if (!StrUtil.isEmptyIfStr(s)) {
//            return R.fail("距离上次修改不足15天");
//        }
        if (userService.updateInfo(id, userInfoVo)) {
            return R.success();
        } else {
            return R.fail("更新失败");
        }
    }

    /**
     * 添加个人资料，
     *
     * @param userInfoVo
     * @return
     * @apiNote 在完成微信官网的注册以后应该调用这个接口
     */
    @Log(module = "用户模块", operation = "新增用户个人信息")
    @PostMapping("/info/add")
    public R addInfo(@RequestBody @Valid UserInfoVo userInfoVo) {
        if (userService.addInfo(UserContext.get().getId(), userInfoVo)) {
            return R.success();
        } else {
            return R.fail("添加失败");
        }
    }


}
