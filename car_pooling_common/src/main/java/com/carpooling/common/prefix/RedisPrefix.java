package com.carpooling.common.prefix;

/**
 * Redis中Key的前缀，统一在这个地方定义为静态常量的String
 *
 * @author LiangHanSggg
 * @date 2023-06-30 18:19
 */
public class RedisPrefix {
    //用户状态
    public static final String USER = "user:";
    // token
    public static final String ACCESS_TOKEN = "token";
    // 问题和解决
    public static final String QA = "qa";
    // 用户信息更新间隔
    public static final String USERINFO_TIME = "user:time:";
    // 反馈的限制时长
    public static final String FEEDBACK_TIME = "user:feedback:";
    // 投诉的限制时长
    public static final String COMPLAIN_TIME = "user:complain:";
    // 黑名单列表
    public static final String BLACKLIST = "user:black";
    // 学生验证的限制时长
    public static final String STUDENT_CERT_TIME = "user:cert:";
    // 电话验证的次数限制
    public static final String PHONE_VERIFY_TIME = "user:phone:";
    // 验证码
    public static final String PHONE_VERIFY_CODE = "user:phone:code:";
    // 订单提交的权限
    public static final String ORDER_ACCESS = "user:order:token:";
    // 创建订单的次数限制
    public static final String ORDER_LIMIT = "user:order:";
    // 发起批次的次数限制
    public static final String BATCH_LIMIT = "user:batch:";
    // 车次的简略信息
    public static final String ORDER_BRIEF_INFO = "order:brief:info:";
    // 自动加入每天的次数限制
    public static final String ORDER_AUTO_JOIN_LIMIT = "user:order:auto:join:";
    // 批次的延时队列
    public static final String BATCH_DELAY_QUEUE = "batch:delay";
}
