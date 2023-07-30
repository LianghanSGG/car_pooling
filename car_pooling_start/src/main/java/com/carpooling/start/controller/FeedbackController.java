package com.carpooling.start.controller;

import com.carpooling.common.annotation.Log;
import com.carpooling.common.pojo.R;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.service.FeedBackService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 *
 * @author LiangHanSggg
 * @date 2023-07-24 21:40
 */
@Validated
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    FeedBackService feedBackService;

    /**
     * 添加反馈
     * @apiNote 不能为空，字数不能超过1K。上传的时候注意是用表单格式的而不是用json格式的
     *
     * @param question 问题的描述
     * @return
     */
    @Log(module = "反馈模块", operation = "创建反馈")
    @PostMapping("/add")
    public R addFeedBack(@NotBlank @Length(max = 10000) String question) {
        Long id = UserContext.get().getId();

        Integer count = redisUtil.StringGet(RedisPrefix.FEEDBACK_TIME + id, Integer.class);
        boolean b;
        if ((b = Objects.nonNull(count)) && count.intValue() == 2) {
            return R.fail("24小时内只能反馈俩次");
        }

        if (feedBackService.addFeedBack(b, question)) {
            return R.success();
        } else {
            return R.fail("新增失败");
        }

    }

    /**
     * 读取回馈的信息。
     *
     * @param feedBackId 要读的反馈id
     * @return
     */
    @Log(module = "反馈模块", operation = "获得反馈通知")
    @GetMapping("/get")
    public R<String> getFeedBack(@NotNull @RequestParam Long feedBackId) {
        String feedBack = feedBackService.getFeedBack(feedBackId);
        if (Objects.isNull(feedBack)) {
            return R.fail("查无回馈");
        } else {
            return R.success(feedBack);
        }
    }


}
