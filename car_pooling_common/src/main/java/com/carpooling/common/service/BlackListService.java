package com.carpooling.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carpooling.common.pojo.db.BlackList;

/**
 * @author LiangHanSggg
 * @date 2023-07-25 19:03
 */
public interface BlackListService extends IService<BlackList> {

    /**
     * 检查是否在黑名单中
     *
     * @param userId
     * @return false不存在 true存在
     */
    boolean checkExist(Long userId);
}
