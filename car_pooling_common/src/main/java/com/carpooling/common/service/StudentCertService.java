package com.carpooling.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carpooling.common.pojo.db.StudentCert;
import com.carpooling.common.pojo.vo.StuCertStateVO;
import com.carpooling.common.pojo.vo.StuCertVO;

/**
 * @author LiangHanSggg
 * @date 2023-07-28 17:13
 */
public interface StudentCertService extends IService<StudentCert> {

    /**
     * 获得信息认证信息
     * @return
     */
    StuCertStateVO checkState();

    /**
     * 检查是否存在待审核或是通过的状态
     * 注意有个中间状态是3。但是基本不可能发生因为有事务。
     * @return
     */
    Integer existReviewed();

    /**
     * 保存或是更新
     *
     * 会进行以下的判断
     * 1. 是否为新用户
     * 2. 该用户的上一条记录是什么状态
     *      1. 不是审核中 ： 直接创建新的。
     *      2. 如果是审核中： 将上一条设置为覆盖状态
     * 根据规则决定是否记录到Redis中，如果是新用户是不会加限制的，
     * 意味着新用户会多一次修改机会，
     * 如果是普通用户的话会有30天的冷却
     * @param stuCertVO
     * @return
     */
    boolean saveStu(StuCertVO stuCertVO);

}
