package com.carpooling.common.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Null;

/**
 * 学生个人资料Vo
 * <p>
 * 可以进行提升的点，可以将user改成基类，现在这个类去继承就可以了。
 *
 * @author LiangHanSggg
 * @date 2023-07-22 19:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVo {

    /**
     * 学号 如果是传递过来无需携带
     *
     */
    @Null(message = "不能修改学号")
    String schoolId;


    /**
     * 昵称
     */
    String nickName;

    /**
     * 电话 如果是传递过来无需携带
     */
    @Null(message = "不能修改电话号")
    String phone;

    /**
     * 性别： 0 女 1 男
     */
    Integer sex;

    /**
     * 微信号
     */
    String account;

    /**
     * 拼单成功次数 如果是传递过来无需携带
     */
    @Null(message = "不能修改成功次数")
    Integer successesNumber;

    /**
     * 拼单总次数 如果是传递过来无需携带
     */
    @Null(message = "不能修改总次数")
    Integer totalNumber;

    /**
     * 信誉度 如果是传递过来无需携带
     */
    @Null(message = "不能修改信誉度")
    Integer reliabilityRating;

    /**
     * 等待时间
     */
    @Min(message = "等待时间不能低于0", value = 0)
    @Max(message = "等待时间不能高于30", value = 30)
    Integer waitTime;

}
