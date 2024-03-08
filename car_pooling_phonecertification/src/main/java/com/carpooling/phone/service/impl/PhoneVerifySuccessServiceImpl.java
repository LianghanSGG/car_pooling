package com.carpooling.phone.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.exception.DException;
import com.carpooling.common.pojo.db.BlackList;
import com.carpooling.common.pojo.db.User;
import com.carpooling.common.pojo.vo.PhoneCodeVerifyVO;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.service.BlackListService;
import com.carpooling.common.service.UserService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.TencentCloudUtil;
import com.carpooling.common.util.UserContext;
import com.carpooling.phone.mapper.PhoneVerifySuccessMapper;
import com.carpooling.phone.pojo.PhoneVerification;
import com.carpooling.phone.service.PhoneVerifySuccessService;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.comm.utils.SmsUtil;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.enumerate.SupplierType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author LiangHanSggg
 * @date 2023-08-04 17:06
 */
@Service
public class PhoneVerifySuccessServiceImpl extends ServiceImpl<PhoneVerifySuccessMapper, PhoneVerification> implements PhoneVerifySuccessService {


    @Autowired
    BlackListService blackListService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserService userService;

    @Autowired
    ApplicationContext applicationContext;


    @Autowired
    private TencentCloudUtil tencentCloudUtil;

    @Override
    public String send(String phone) {
        Long userId = UserContext.get().getId();
        String key = "restricted_code" + userId + "";

        if (redisUtil.StringGet(key, String.class) != null) {
            throw new DException("被限制发送次数");
        }
        String dayNumKey = "cur_day_code_num" + userId + "";
        Integer num = redisUtil.StringGet(dayNumKey, Integer.class);
        if (num != null && num >= 4) {
            redisUtil.StringAdd(key, "RESTRICTED", 24L, TimeUnit.HOURS);
            throw new DException("今日次数已达上线");
        }
        // 生成六位验证码
        StringBuffer code = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int r = random.nextInt(10);
            code.append(r);
        }
        String phoneCodeKey = "userphonecode:" + userId + ":" + phone;
        redisUtil.StringAdd(phoneCodeKey, code.toString(), 900L, TimeUnit.SECONDS);
        tencentCloudUtil.sendPhoneCode(phone, code.toString());
        if (num == null) {
            // 当天剩余时间
            LocalDateTime midnight = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), midnight);
            redisUtil.StringAdd(dayNumKey, 1, seconds, TimeUnit.SECONDS);
        } else {
            redisUtil.StringIncrement(dayNumKey, 1L);
        }
        return "成功发送";
    }


    // 无需做俩次短信之间cd的检查，让前端去做，如果是故意的话自己次数也完了
//    ====================================================下面是旧版的
//    @Override
//    public String send(String phone) {
//        int state = UserContext.get().getState().intValue();
//        Long id = UserContext.get().getId();
//        // i==0意味着是正常用户
//        if (state == 0) {
//            // 正常用户进行修改，先判断上一条成功的距离现在是否有半年，如果有的话的就判断次数
//            LambdaQueryWrapper<PhoneVerification> last = Wrappers.lambdaQuery(PhoneVerification.class)
//                    .eq(PhoneVerification::getUserId, id)
//                    .eq(PhoneVerification::getVerifySuccess, 1)
//                    .orderByDesc(PhoneVerification::getCreateTime)
//                    .last("limit 1");
//
//            PhoneVerification lastSuccess = getOne(last);
//
//            if (Objects.isNull(lastSuccess)) return "联系管理员";
//
//            Duration between = LocalDateTimeUtil.between(lastSuccess.getCreateTime(), LocalDateTime.now());
//            long days = between.toDays();
//            //接近半年
//            if (days > 180) {
//                return "距离上次修改不足180天";
//            }
//            // A是最后一次拉黑的时间，B是最后一次认证成功的时间
//            // A==null   B
//            // A!=null A/B
//            // ------》时间线
//            // A B ---C(now)
//            // B A ---C(now)
//            // A/B ---C(now) 计数
//
//            long number = 0;
//            //检查是否被拉黑
//            LocalDateTime time = beShielded(id);
//
//            if (time == null || lastSuccess.getCreateTime().isAfter(time)) {
//                //没有被拉黑过 或 B在A的后面
//                LambdaQueryWrapper<PhoneVerification> gt = Wrappers.lambdaQuery(PhoneVerification.class)
//                        .eq(PhoneVerification::getUserId, id)
//                        .gt(PhoneVerification::getCreateTime, lastSuccess.getCreateTime());
//                number = count(gt);
//            } else {
//                number = countNumberBeforeShielded(id, time);
//            }
//
//            return judgeByCount(id, phone, number);
//            // 下面是优雅小技巧
////            return judgeByCount(id, phone, (time == null || lastSuccess.getCreateTime().isAfter(time)) ? count(Wrappers.lambdaQuery(PhoneVerification.class)
////                    .eq(PhoneVerification::getUserId, id)
////                    .gt(PhoneVerification::getCreateTime, lastSuccess)) : countNumberBeforeShielded(id, time));
//
//        }
//
//        return newUserResidueDegree(id, phone);
//    }
//
    @Override
    public String verify(PhoneCodeVerifyVO phoneCodeVerifyVO) {
        Long userId = UserContext.get().getId();

        String phoneCodeKey = "userphonecode:" + userId + ":" + phoneCodeVerifyVO.getPhone();
        String codeString = redisUtil.StringGet(phoneCodeKey, String.class);

        if (Objects.isNull(codeString)) {
            throw new DException("验证码已过期");
        }

        if (!codeString.equals(phoneCodeVerifyVO.getCode())) throw new DException("验证码错误");

//        LambdaQueryWrapper<PhoneVerification> last = Wrappers.lambdaQuery(PhoneVerification.class)
//                .eq(PhoneVerification::getUserId, userId)
//                .orderByDesc(PhoneVerification::getCreateTime)
//                .last("limit 1");

//        PhoneVerification one = getOne(last);

//        if (Objects.isNull(one)) throw new DException("请联系管理员");

//        if (!one.getUserPhone().equals(phoneCodeVerifyVO.getPhone())) throw new DException("号码与验证码不一致");

        LambdaUpdateWrapper<User> set = Wrappers.lambdaUpdate(User.class)
                .eq(User::getId, UserContext.get().getId())
                .set(User::getPhone, phoneCodeVerifyVO.getPhone());
        return userService.update(set) ? "验证成功" : "验证失败";
    }

//    @Transactional(rollbackFor = RuntimeException.class)
//    public boolean successVerify(PhoneVerification one, String phone) {
//        one.setVerifySuccess(1);
//        boolean b = updateById(one);
//        LambdaUpdateWrapper<User> set = Wrappers.lambdaUpdate(User.class)
//                .eq(User::getId, UserContext.get().getId())
//                .set(User::getPhone, phone);
//        boolean update = userService.update(set);
//        return b && update;
//    }


    /**
     * 不是认证过的用户，包括新用户，拉黑过的用户，发送过几次的用户，发送短信
     *
     * @param userId
     * @return
     */
    public String newUserResidueDegree(Long userId, String phone) {

        Integer integer = redisUtil.StringGet(RedisPrefix.PHONE_VERIFY_TIME + userId, Integer.class);
        if (Objects.isNull(integer)) {

            //来到这里意味着要么是、超过时间请求间隔、是没有请求过、或是剩下的次数不够3次。

            //先检查是不是新人第一次请求
            long requestCount = count(Wrappers.lambdaQuery(PhoneVerification.class)
                    .eq(PhoneVerification::getUserId, userId));
            if (requestCount == 0) {
                //新人
                redisUtil.StringAdd(RedisPrefix.PHONE_VERIFY_TIME + userId, 3, 1, TimeUnit.DAYS);
                return sendMes(userId, phone, true);
            }

            //不是新人。那么要检查是否被拉黑过，拉黑的要从拉黑的次数开始算起。没有被拉黑次数就是requestCount
            //检查是否被拉黑过
            LocalDateTime time = beShielded(userId);

            if (time != null) {
                //被拉黑
                requestCount = countNumberBeforeShielded(userId, time);
            }


            return judgeByCount(userId, phone, requestCount);
        }

        return integer.intValue() == 0 ? "24小时内限制3条短信" : sendMes(userId, phone, true);

    }

    /**
     * 计算次数，从拉黑之后到现在的次数
     *
     * @param userId 用户的id
     * @param time   最后一次被拉黑的事件
     * @return
     */
    public long countNumberBeforeShielded(Long userId, LocalDateTime time) {
        LambdaQueryWrapper<PhoneVerification> gt = Wrappers.lambdaQuery(PhoneVerification.class)
                .eq(PhoneVerification::getUserId, userId)
                .gt(PhoneVerification::getCreateTime, time);

        return count(gt);
    }

    /**
     * 根据请求次数分别对应实现不同的发送策略
     *
     * @param userId
     * @param phone
     * @param count
     * @return
     */
    public String judgeByCount(Long userId, String phone, long count) {
        if (count == 5) {
            //到达上限直接拉黑
            Shielded(userId);
            return "账号已被锁定，请联系管理员";
        }

        if (count <= 2) {
            redisUtil.StringAdd(RedisPrefix.PHONE_VERIFY_TIME + userId, 3, 1, TimeUnit.DAYS);
            return sendMes(userId, phone, true);
        }
        return sendMes(userId, phone, false);
    }


    /**
     * 检查是否被拉黑过
     *
     * @param userId
     * @return null 没有被拉黑过， 返回时间就是最后一次被拉黑的时间
     */
    public LocalDateTime beShielded(Long userId) {
        LambdaQueryWrapper<PhoneVerification> last = Wrappers.lambdaQuery(PhoneVerification.class)
                .eq(PhoneVerification::getUserId, userId)
                .eq(PhoneVerification::getVerifySuccess, 2)
                .orderByDesc(PhoneVerification::getCreateTime)
                .last("limit 1");
        PhoneVerification one = getOne(last);
        return Objects.isNull(one) ? null : one.getCreateTime();
    }

    /**
     * 拉黑
     * 先把最后一条记录的属性改为2，然后将这个userid插入到黑名单中.
     * updateAndInsert 方法是减少长事务，application是防止事务失效。
     *
     * @param userId
     */
    public void Shielded(Long userId) {
        // 要先将最后一条的id查出来，然后去更新，然后加入到黑名单;
        LambdaQueryWrapper<PhoneVerification> last = Wrappers.lambdaQuery(PhoneVerification.class)
                .eq(PhoneVerification::getUserId, userId)
                .orderByDesc(PhoneVerification::getCreateTime)
                .last("limit 1");
        PhoneVerification one = getOne(last);
        applicationContext.getBean(this.getClass()).updateAndInsert(one, userId);
        redisUtil.AsyncDeleted(RedisPrefix.PHONE_VERIFY_TIME + userId);
        redisUtil.AsyncDeleted(RedisPrefix.PHONE_VERIFY_CODE + userId);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public void updateAndInsert(PhoneVerification phoneVerification, Long userId) {
        phoneVerification.setVerifySuccess(2);
        updateById(phoneVerification);
        BlackList blackList = new BlackList();
        blackList.setId(userId);
        blackListService.save(blackList);
    }

    /**
     * 发送短信
     * 成功之后会对redis进行扣减，会将验证码放入到redis,会在数据库中创建一行记录
     *
     * @param userId
     * @param phone
     * @param useCache 是否对对应的间隔限制减1
     * @return
     */
    public String sendMes(Long userId, String phone, boolean useCache) {
        String randomInt = SmsUtil.getRandomInt(6);
        SmsResponse smsResponse = SmsFactory.createSmsBlend(SupplierType.TENCENT).sendMessage("86" + phone, randomInt);
        if ("200".equals(smsResponse.getCode())) {
            // 将验证码加入到缓存中。并且在数据库中创建一条新的记录
            PhoneVerification phoneVerification = new PhoneVerification();
            phoneVerification.setUserPhone(phone);
            phoneVerification.setUserId(userId);

            if (save(phoneVerification)) {
                // 验证码
                redisUtil.StringAdd(RedisPrefix.PHONE_VERIFY_CODE + userId, randomInt, 15, TimeUnit.MINUTES);
                if (useCache) {
                    //次数和时间限制
                    redisUtil.StringDecrement(RedisPrefix.PHONE_VERIFY_TIME + userId, 1);
                }
                return "发送成功";
            }
        }
        return "发送失败";
    }
}
