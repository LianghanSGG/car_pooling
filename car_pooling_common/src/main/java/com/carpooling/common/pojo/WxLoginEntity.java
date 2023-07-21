package com.carpooling.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author LiangHanSggg
 * @date 2023-07-20 16:23
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WxLoginEntity {

    String openid;

    String session_key;

    String unionid;

    Integer errcode;

    String errmsg;

}
