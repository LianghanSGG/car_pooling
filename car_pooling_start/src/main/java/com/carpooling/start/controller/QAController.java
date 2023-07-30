package com.carpooling.start.controller;

import com.carpooling.common.pojo.R;
import com.carpooling.common.pojo.db.Question;
import com.carpooling.start.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * @author LiangHanSggg
 * @date 2023-07-23 16:36
 */
@RestController
@RequestMapping("/qa")
public class QAController {

    @Autowired
    QuestionService questionService;

    /**
     * 获得问题和答复
     * @return
     */
    @GetMapping("/getInfo")
    public R<List<Question>> getQA() {
        return R.success(questionService.getInfo());
    }



}
