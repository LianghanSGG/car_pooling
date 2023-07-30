package com.carpooling.start.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carpooling.common.pojo.db.Question;

import java.util.List;

/**
 * @author LiangHanSggg
 * @date 2023-07-23 16:34
 */
public interface QuestionService extends IService<Question> {

    List<Question> getInfo();

}
