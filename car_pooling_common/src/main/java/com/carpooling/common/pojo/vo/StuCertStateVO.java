package com.carpooling.common.pojo.vo;

import lombok.Data;

/**
 * 学生认证信息VO
 *
 * @author LiangHanSggg
 * @date 2023-07-28 17:16
 */
@Data
public class StuCertStateVO {

    /**
     * 现在的状态 0待审核 1 通过 2 未通过  3 未发起审核
     */
    Integer state;

    /**
     * 真实姓名 不一定有
     */
    String name;

    /**
     * 学号 不一定有
     */
    String schoolId;

    /**
     * 如果出现问题的回馈   不一定有
     */
    String message;

}
