package com.carpooling.phone.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carpooling.common.pojo.vo.PhoneCodeVerifyVO;
import com.carpooling.phone.pojo.PhoneVerification;

/**
 * @author LiangHanSggg
 * @date 2023-08-04 17:05
 */
public interface PhoneVerifySuccessService extends IService<PhoneVerification> {

    /**
     * 发送短信验证码
     * @param phone
     * @return
     */
    String send(String phone);

    /**
     * 校验验证码
     * @param phoneCodeVerifyVO
     * @return
     */
    String verify(PhoneCodeVerifyVO phoneCodeVerifyVO);
}
