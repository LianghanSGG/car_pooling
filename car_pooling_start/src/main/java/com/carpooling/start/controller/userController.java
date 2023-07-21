package com.carpooling.start.controller;

import cn.hutool.core.util.StrUtil;
import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.vo.LoginVo;
import com.carpooling.common.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author LiangHanSggg
 * @date 2023-07-20 16:29
 */
@RequestMapping("/user")
public class userController {




    @GetMapping("/login")
    public R<LoginVo> WxLogin(@RequestParam("code") String code) {
        if (StrUtil.isEmptyIfStr(code)) {
            return R.fail("code不能为空");
        }

        return R.success(null);
    }


}
