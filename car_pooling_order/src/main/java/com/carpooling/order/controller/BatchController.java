package com.carpooling.order.controller;

import com.carpooling.common.annotation.Log;
import com.carpooling.common.annotation.PreCheck;
import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.vo.BatchVO;
import com.carpooling.common.pojo.vo.ShoppingCarVo;
import com.carpooling.common.pojo.vo.UserSimpleInfoVO;
import com.carpooling.common.service.BlackListService;
import com.carpooling.common.service.impl.BatchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * 批次模块
 *
 * @author LiangHanSggg
 * @date 2023-08-02 21:24
 */
@Validated
@RestController
@RequestMapping("/order")
public class BatchController {


    @Autowired
    BatchServiceImpl batchService;

    @Autowired
    BlackListService blackListService;

    /**
     * 获得批次
     *
     * @return
     * @apiNote 用于得到还处于待确认状态的批次
     */
    @Log(module = "批次模块", operation = "获得批次信息")
    @GetMapping("/batch")
    public R<List<BatchVO>> getBatch() {
        List<BatchVO> batchList = batchService.getBatchList();
        if (Objects.isNull(batchList)) {
            return R.fail("没有发起拼单");
        } else {
            return R.success(batchList);
        }
    }

    /**
     * 创建拼车记录
     *
     * @param shoppingCarVo
     * @return
     * @apiNote 注意这个不是创建成为团主的接口
     */
    @PreCheck(onlyBlackList = false,studentStart = true)
    @Log(module = "批次模块", operation = "创建批次")
    @PostMapping("/batch/create")
    public R createBatch(@RequestBody @Valid ShoppingCarVo shoppingCarVo) {

        return R.success(batchService.createBatch(shoppingCarVo));
    }

    /**
     * 检查是否有乘客申请
     *
     * @return
     */
    @Log(module = "批次模块", operation = "检查是否有乘客申请")
    @GetMapping("/batch/list")
    public R<List<UserSimpleInfoVO>> getUserRequest() {
        return R.success(batchService.listRequest());
    }

    /**
     * 通过用户的申请请求
     *
     * @param orderBatchId
     * @return
     */
    @PreCheck(onlyBlackList = false,studentStart = true)
    @Log(module = "批次模块", operation = "允许用户加入订单")
    @GetMapping("/batch/pass")
    public R passCheck(@RequestParam Long orderBatchId) {
        if (batchService.passReq(orderBatchId)) {
            return R.success("通过");
        } else {
            return R.fail("失败");
        }
    }

    /**
     * 拒绝用户的申请请求
     *
     * @param orderBatchId
     * @return
     */
    @PreCheck(onlyBlackList = false,studentStart = true)
    @Log(module = "批次模块", operation = "拒绝用户加入订单")
    @GetMapping("/batch/refuse")
    public R refuseCheck(@RequestParam Long orderBatchId) {
        if (batchService.refuseReq(orderBatchId)) {
            return R.success("通过");
        } else {
            return R.fail("失败");
        }
    }
}
