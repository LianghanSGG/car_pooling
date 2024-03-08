package com.carpooling.common.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 乘客信息
 *
 * @author LiangHanSggg
 * @date 2023-08-15 14:19
 */
@Data
@Accessors(chain = true)
public class PassengerVO {

    /**
     * 乘客id
     */
    Long userId;

    /**
     * 乘客姓名
     */
    String userName;

    /**
     * 乘客性别
     */
    Integer sex;

    /**
     * 乘客电话
     */
    String phone;

    /**
     * 乘客微信号
     */
    String wxAccount;

    /**
     * 是否是拼主 0不是 1是
     */
    Integer owner;

}
