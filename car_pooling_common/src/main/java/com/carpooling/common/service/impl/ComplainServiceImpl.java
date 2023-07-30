package com.carpooling.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.mapper.ComplainMapper;
import com.carpooling.common.pojo.db.Complain;
import com.carpooling.common.pojo.vo.ComplainVO;
import com.carpooling.common.pojo.vo.UserVO;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.service.ComplainService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 可以考虑将反馈和投诉合成一张表、一个业务逻辑。
 * 大差不差
 *
 * @author LiangHanSggg
 * @date 2023-07-25 16:06
 */
@Slf4j
@Service
public class ComplainServiceImpl extends ServiceImpl<ComplainMapper, Complain> implements ComplainService {

    @Autowired
    RedisUtil redisUtil;

    @Override
    public boolean addComplain(boolean exist, ComplainVO complainVO) {
        Complain complain = new Complain();
        BeanUtil.copyProperties(complainVO, complain);

        UserVO userVO = UserContext.get();
        complain.setUserId(userVO.getId());
        complain.setUserOpenid(userVO.getOpenid());

        if (!save(complain)) return false;


        if (exist) {
            redisUtil.StringIncrement(RedisPrefix.COMPLAIN_TIME + userVO.getId(), 1);
        } else {
            redisUtil.StringAdd(RedisPrefix.COMPLAIN_TIME + userVO.getId(), 1, 1, TimeUnit.DAYS);
        }

        return true;
    }


    @Override
    public String getReplay(Long complainId) {
        UserVO userVO = UserContext.get();
        LambdaQueryWrapper<Complain> eq = Wrappers.lambdaQuery(Complain.class).eq(Complain::getId, complainId)
                .eq(Complain::getUserId, userVO.getId());

        Complain one = getOne(eq);
        if (Objects.isNull(one)) return null;

        if (one.getAccepted().intValue() == 2) {
            return one.getReply();
        }
        one.setAccepted(2);

        if (!updateById(one)) {
            log.info("读取投诉回馈，更新状态出现异常,id:{}", complainId);
        }


        return one.getReply();
    }
}
