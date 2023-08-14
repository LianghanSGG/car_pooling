package com.carpooling.common.annotation;

import com.carpooling.common.exception.PreCheckException;
import com.carpooling.common.pojo.vo.UserVO;
import com.carpooling.common.service.BlackListService;
import com.carpooling.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 同一个接口使用AOP加黑名单Redis校验需要实测：不校验80-90ms 校验 120-140ms
 * 优化方案： 不使用AOP---代码冗余
 * 黑名单前置到进程---布隆过滤器---一致性问题  接近不校验的水平O(1)
 *
 * @author LiangHanSggg
 * @date 2023-08-06 17:54
 */
@Aspect
@Component
@Slf4j
public class PreCheckAspect {

    @Autowired
    BlackListService blackListService;


    @Before("@annotation(preCheck)")
    public void beforeCheck(PreCheck preCheck) {

        UserVO userVO = UserContext.get();
        Long id = userVO.getId();

        if (blackListService.checkExist(id)) throw new PreCheckException("已拉黑，联系管理员");


        if (!preCheck.onlyBlackList()) {
            int state = userVO.getState().intValue();
            if (state == 3) throw new PreCheckException("还未进行学生认证");
            if (state == 2) throw new PreCheckException("还未进行电话认证");
            if (state == 4) throw new PreCheckException("未完成学生和电话认证");
            if (state == 1) throw new PreCheckException("请先注册");
        }

    }

}
