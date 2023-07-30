package com.carpooling.start.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.pojo.db.Question;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.start.mapper.QuestionsMapper;
import com.carpooling.start.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author LiangHanSggg
 * @date 2023-07-23 16:35
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionsMapper, Question> implements QuestionService {

    @Autowired
    RedisUtil redisUtil;

    //后期检查的时候看看是否存在问题
    @Override
    public List<Question> getInfo() {

        List<Question> list = redisUtil.StringGet(RedisPrefix.QA, List.class);
        if (list == null || list.size() == 0) {
            List<Question> list1 = list();
            redisUtil.StringAdd(RedisPrefix.QA, list1, RandomUtil.randomInt(2, 10), TimeUnit.DAYS);
            return list1;

        } else {
            return list;
        }

    }
}
