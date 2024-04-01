package com.carpooling.stu.controller;

import com.carpooling.common.annotation.Log;
import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.vo.StuCertStateVO;
import com.carpooling.common.pojo.vo.StuCertVO;
import com.carpooling.common.service.StudentCertService;
import com.carpooling.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * @author LiangHanSggg
 * @date 2023-07-28 17:09
 */
@Validated
@RestController
@RequestMapping("/stu")
public class StuController {

    @Autowired
    StudentCertService studentCertService;

    @Autowired
    RedisUtil redisUtil;

    /**
     * 查看验证状态
     *
     * @return
     */
    @Log(module = "学生认证模块", operation = "查看状态")
    @GetMapping("/state")
    public R<StuCertStateVO> getState() {
        return R.success(studentCertService.checkState());
    }


    /**
     * 检查是否存在待审核或是通过的状态
     *
     * @return 0不存在 1已存在
     * @apiNote 可以用于更新的时候进行提示，比如：现在正处于审核阶段。
     */
    @Log(module = "学生认证模块", operation = "检查是否存在待审核")
    @GetMapping("/exist")
    public R<Integer> existReviewed() {
        return R.success(studentCertService.existReviewed());
    }

    /**
     * 这个接口会实现保存或是更新的学生的功能
     *
     * @param stuCertVO
     * @return
     * @apiNote 新用户创建之后可以有一次的修改机会。但是发起第二次后就会开始倒计时了，得30天后才可以更新
     */
    @Log(module = "学生认证模块", operation = "发起学生认证")
    @PostMapping("/info")
    public R saveOrUpdate(@Valid @RequestBody StuCertVO stuCertVO) {

//        Long id = UserContext.get().getId();
//        String s = redisUtil.StringGet(RedisPrefix.STUDENT_CERT_TIME + id, String.class);
//        if (Objects.nonNull(s)) return R.fail("30天只能修改一次，有异议发起反馈");

        if (studentCertService.saveStu(stuCertVO)) {
            return R.success();
        } else {
            return R.fail("失败");
        }

    }

    /**
     * 获得上传的权限
     *
     * @param type
     * @return
     * @apiNote 权限默认是10分钟的有效期，自己测试的时候要试试看如果过期了会怎么样了,记住要限制上传图片的大小！！！！
     * 图片要上传到七牛云的图片存储服务，需要先来这里获得token。拿着token去七牛云上传然后七牛会返回的参数叫做key。key就是文件的路径和名字，拿着key和(CDNAddress)拼接就可以访问了。
     * 一般看到图片会消耗掉流量，流量是要钱的，因此如果你想看看上传是否成功你得找我要一个前缀(CDNAddress)你才能看到图片
     */
    @Log(module = "学生认证模块", operation = "获得图片上传权限")
    @GetMapping("/pic")
    public R<String> getPicToken(@NotEmpty(message = "type不能为空")
                                 @Size(max = 10, message = "长度不能过长")
                                 @RequestParam("type") String type) {
        String picToken = studentCertService.getPicToken(type);
        if (Objects.isNull(picToken)) return R.fail("失败");

        return R.success(picToken);
    }


}
