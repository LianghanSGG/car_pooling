package com.carpooling.phone.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangHanSggg
 * @date 2023-08-04 16:55
 */
@TableName("phone_verification")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneVerification extends BaseEntity {

    @TableId
    Long id;

    /**
     * 用户id
     */
    Long userId;

    /**
     * 用户电话
     */
    String userPhone;

    /**
     * 验证是否成功 0还未成功 1成功 2被拉黑
     */
    Integer verifySuccess;


}
