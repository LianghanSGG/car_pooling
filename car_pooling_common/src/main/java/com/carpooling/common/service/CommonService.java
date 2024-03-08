package com.carpooling.common.service;

/**
 * 可以将这个做成模板方法+策略模式。
 *
 * @author LiangHanSggg
 * @date 2023-08-11 17:02
 */
public interface CommonService {
    /**
     * 实现次数限制、防重提交的校验。
     *
     * @param userId      用户的id
     * @param redisKey    次数校验的redis前缀
     * @param msg         次数校验不通过时要提示的内容
     * @param accessToken 权限token
     * @return 次数限制的redis中存在的值
     */
    Integer checkAndQualificationCancel(Long userId, String redisKey, String msg, String accessToken);

    /**
     * 下单后的状态更新
     * 不存再则会在redis中创建，如果存在的话会自减1.
     *
     * @param qualification 资质核销时得到的值
     * @param redisKey      需要更新的Key
     * @param num           需要限制的次数（假设次数4那么设置为3）
     * @param time          限制的时间间隔数，单位是小时
     */
    void afterPlaceOrder(Integer qualification, String redisKey, int num, int time);
}
