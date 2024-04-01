package com.carpooling.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.mapper.FeedBackMapper;
import com.carpooling.common.pojo.db.FeedBack;
import com.carpooling.common.service.FeedBackService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author LiangHanSggg
 * @date 2023-07-24 21:46
 */
@Slf4j
@Service
public class FeedBackServiceImpl extends ServiceImpl<FeedBackMapper, FeedBack> implements FeedBackService {

    @Autowired
    RedisUtil redisUtil;

    @Override
    public boolean addFeedBack(boolean exist, String question) {
        FeedBack feedBack = new FeedBack();
        feedBack.setUserOpenid(UserContext.get().getOpenid());
        feedBack.setQuestion(question);
        if (!save(feedBack)) return false;

//        if (exist) {
//            redisUtil.StringIncrement(RedisPrefix.FEEDBACK_TIME + UserContext.get().getId(), 1);
//        } else {
//            redisUtil.StringAdd(RedisPrefix.FEEDBACK_TIME + UserContext.get().getId(), 1, 1, TimeUnit.DAYS);
//        }
        return true;
    }

    @Override
    public String getFeedBack(Long feedBackId) {
        String openid = UserContext.get().getOpenid();
        LambdaQueryWrapper<FeedBack> eq = Wrappers.lambdaQuery(FeedBack.class)
                .eq(FeedBack::getUserOpenid, openid)
                .eq(FeedBack::getId, feedBackId);
        FeedBack one = getOne(eq);
        if (Objects.isNull(one)) return null;

        // 读过一次
        if (one.getAccepted().intValue() == 2) {
            return one.getReply();
        }

        LambdaUpdateWrapper<FeedBack> eq1 = Wrappers.lambdaUpdate(FeedBack.class).set(FeedBack::getAccepted, 2).eq(FeedBack::getId, feedBackId);
        if (!update(eq1)) {
            log.info("读取回馈，更新状态出现异常,id:{}", feedBackId);
        }
        return one.getReply();


    }
}