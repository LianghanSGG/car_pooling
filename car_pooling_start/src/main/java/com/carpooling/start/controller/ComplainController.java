package com.carpooling.start.controller;

import com.carpooling.common.annotation.Log;
import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.vo.ComplainVO;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.service.ComplainService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author LiangHanSggg
 * @date 2023-07-25 16:11
 */
@Validated
@RestController
@RequestMapping("/complain")
public class ComplainController {

    @Autowired
    ComplainService complainService;

    @Autowired
    RedisUtil redisUtil;

    /**
     * 发起投诉
     * @param complainVO
     * @return
     */
    @Log(module = "投诉模块", operation = "发起投诉")
    @PostMapping("/add")
    public R addComplain(@Valid @RequestBody ComplainVO complainVO) {
        Long id = UserContext.get().getId();

        Integer count = redisUtil.StringGet(RedisPrefix.COMPLAIN_TIME + id, Integer.class);
        boolean b;
        if ((b = Objects.nonNull(count)) && count.intValue() == 3) {
            return R.fail("24小时内只能投诉3次");
        }

        if (complainService.addComplain(b, complainVO)) {
            return R.success();
        } else {
            return R.fail("新增失败");
        }
    }

    /**
     * 获得投诉
     * @apiNote 暂时只返回回复结果，等后面根据需求进行修改
     *
     * @param complainId
     * @return
     */
    @Log(module = "投诉模块", operation = "获得投诉通知")
    @GetMapping("/get")
    public R<String> getReplay(@NotNull @RequestParam Long complainId) {
        String replay = complainService.getReplay(complainId);
        if (Objects.isNull(replay)) {
            return R.fail("查无回馈");
        } else {
            return R.success(replay);
        }
    }

}
