package com.carpooling.order.controller;


import cn.hutool.core.date.LocalDateTimeUtil;
import com.carpooling.common.annotation.Log;
import com.carpooling.common.annotation.PreCheck;
import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.db.OrderUser;
import com.carpooling.common.pojo.vo.*;
import com.carpooling.common.service.BlackListService;
import com.carpooling.common.service.OrderService;
import com.carpooling.common.service.OrderUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单模块
 *
 * @author LiangHanSggg
 * @date 2023-07-25 19:48
 */
@Validated
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderUserService orderUserService;

    @Autowired
    OrderService orderService;

    @Autowired
    BlackListService blackListService;

    /**
     * 获得历史订单
     *
     * @param index
     * @param page
     * @return
     * @apiNote 不知道需要什么，暂时发送这些，后期根据需要更改字段
     */
    @Log(module = "订单模块", operation = "获得历史订单")
    @GetMapping("/history")
    public R<List<OrderUser>> getHistory(@RequestParam("index") @Min(1) @Max(50) int index,
                                         @RequestParam("page") @Min(1) @Max(10) int page) {
        return R.success(orderUserService.getHistory(index, page));

    }

    /**
     * 用于获得首页的订单列表
     *
     * @param index
     * @param page
     * @return
     * @apiNote 注意字段里面有一个是否属于自己！如果订单中有一个是属于自己的，应该做出特殊标识。
     * 这个接口用于已经注册了的用户查看，另一些没有登录的用户调用另一个获得订单列表的接口
     */
    @Log(module = "订单模块", operation = "获得订单列表")
    @GetMapping("/orderlist")
    public R<List<OrderInfoVO>> getOrderList(@RequestParam("index") @Min(1) @Max(50) int index,
                                             @RequestParam("page") @Min(1) @Max(10) int page) {
        return R.success(orderService.orderList(index, page));
    }


    /**
     * 条件查询
     */
    @Log(module = "订单模块", operation = "条件查询订单列表")
    @PostMapping("/orderlist")
    public R<List<OrderInfoVO>> conditionOrderList(@Valid @RequestBody OderListConditionVO oderListConditionVO) {
        int i = oderListConditionVO.getSex().intValue();
        if (i == 0 || i == 1 || i == 2) {
            return R.success(orderService.conditionOrder(oderListConditionVO));
        } else {
            return R.fail("参数有误");
        }
    }

    /**
     * 获得提交的权限
     *
     * @return
     * @apiNote 只要涉及到订单提交的都得在这里申请权限
     * 有效期是15分钟。在提交订单之前得先请求这个方法获得token,如果成功的话data就是token。失败的话data是没有值的。
     */
    @PreCheck(onlyBlackList = false)
    @Log(module = "订单模块", operation = "获得提交订单的的token")
    @GetMapping("/accesstoken")
    public R<String> getAccessToken() {
        return R.success(orderService.getToken());
    }


    // TODO: 2023/8/12 功能测试没有问题，差逻辑测试

    /**
     * 创建属于自己的订单
     *
     * @param placeOrderVO
     * @return
     */
    @PreCheck(onlyBlackList = false)
    @Log(module = "订单模块", operation = "创建订单")
    @PostMapping("/create")
    public R<String> createOrder(@Valid @RequestBody PlaceOrderVO placeOrderVO) {
        if (!LocalDateTimeUtil.isSameDay(placeOrderVO.getLatestTime(), placeOrderVO.getEarliestTime()))
            return R.fail("最早出发和最晚出发应该在同一天");

        LocalDateTime now = LocalDateTime.now();
        if (LocalDateTimeUtil.between(now, placeOrderVO.getLatestTime()).toMinutes() < 15) {
            return R.fail("请至少提前15分钟预约");
        }

        if (LocalDateTimeUtil.between(now, placeOrderVO.getLatestTime()).toHours() >= 96) {
            return R.fail("不能创建最晚发车时间距今超过4天的单子");
        }
        return R.success(orderService.createOrder(placeOrderVO));
    }


    /**
     * 加入自动加入的单子
     *
     * @param autoJoinVO
     * @return
     */
    @PreCheck(onlyBlackList = false)
    @Log(module = "订单模块", operation = "加入自动加入单子")
    @PostMapping("/join")
    public R joinOrder(@Valid @RequestBody AutoJoinVO autoJoinVO) {
        return R.success(orderService.authJoin(autoJoinVO));
    }

    // TODO: 2023/8/12 未测试

    /**
     * 获得推荐车次
     *
     * @param recommendVO
     * @return
     * @apiNote 这个接口用于在创建订单之前根据用户选择的时间进行推荐，推荐的条件是根据订单的出发时间，结束时间，和用户自定义的推荐时间来进行推荐。没有其他的筛选条件
     */
    @Log(module = "订单模块", operation = "获得推荐车次")
    @GetMapping("/getRecommendedList")
    public R<List<OrderInfoVO>> getRecommendedList(@Valid @RequestBody RecommendVO recommendVO) {
        return R.success(orderService.getRecommended(recommendVO));
    }
}
