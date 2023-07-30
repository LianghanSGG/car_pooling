package com.carpooling.order.controller;


import com.carpooling.common.annotation.Log;
import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.db.OrderUser;
import com.carpooling.common.service.OrderUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * @author LiangHanSggg
 * @date 2023-07-25 19:48
 */
@Validated
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderUserService orderUserService;

    /**
     * 获得历史订单
     * @param index
     * @param page
     * @return
     */
    @Log(module = "订单模块", operation = "获得历史订单")
    @GetMapping("/history")
    public R<List<OrderUser>> getHistory(@RequestParam("index") @Min(1) @Max(50) int index,
                                         @RequestParam("page") @Min(1) @Max(10) int page) {
        List<OrderUser> history = orderUserService.getHistory(index, page);
        if (history == null || history.isEmpty()) {
            return R.fail("还没有拼单");
        } else {
            return R.success(history);
        }
    }


}
