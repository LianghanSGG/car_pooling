package com.carpooling.start.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.carpooling.common.annotation.Log;
import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.db.Complain;
import com.carpooling.common.pojo.vo.ComplainVO;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.service.ComplainService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.SensitiveFilterService;
import com.carpooling.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;

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
     *
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
     *
     * @param complainId
     * @return
     * @apiNote 暂时只返回回复结果，等后面根据需求进行修改
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

    /**
     * 检查是否存在违法词。
     * 传过来的数据是一个对象，对象里面要有一个key是txt。值是要检查的字符串
     * 返回一个对象。其中state =1时，代表存在违法词。0是没有。如果有违法词的话是会有keyword
     *
     * @param map
     * @return
     */
    @PostMapping("/verfiy")
    public R getverfiy(@RequestBody Map<String, String> map) {
        String txt = map.get("txt");
        SensitiveFilterService filter = SensitiveFilterService.getInstance();
        boolean existKeyWord = filter.checkContainCount(txt);
        Map<String, String> res = new HashMap<>();
        if (existKeyWord) {
            res.put("state", 1 + "");
            res.put("keyword", filter.returnSensitiveWord(txt));
        } else {
            res.put("state", 0 + "");
        }
        return R.success(res);
    }

    /**
     * 检查是否有投诉回信
     *
     * @return
     */
    @GetMapping("/check")
    public R<List<String>> checkComplain() {
        Long userId = UserContext.get().getId();
        LambdaQueryWrapper<Complain> eq = Wrappers.lambdaQuery(Complain.class)
                .eq(Complain::getUserId, userId)
                .eq(Complain::getAccepted, 1)
                .select(Complain::getId);
        List<Complain> list = complainService.list(eq);
        List<String> res = new LinkedList<>();

        list.forEach(e -> {
            String s = String.valueOf(e.getId());
            res.add(s);
        });

        return R.success(res);
    }
}
