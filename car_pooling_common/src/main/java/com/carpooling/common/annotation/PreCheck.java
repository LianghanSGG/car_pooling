package com.carpooling.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实现对状态的检查，检查内容、黑名单，电话验证，学生认证
 *
 * @author LiangHanSggg
 * @date 2023-08-06 17:52
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreCheck {

    boolean onlyBlackList() default true;
}
