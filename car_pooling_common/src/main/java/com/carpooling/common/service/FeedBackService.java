package com.carpooling.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carpooling.common.pojo.db.FeedBack;

/**
 * @author LiangHanSggg
 * @date 2023-07-24 21:41
 */
public interface FeedBackService extends IService<FeedBack> {

    boolean addFeedBack(boolean exist, String question);

    String getFeedBack(Long feedBackId);
}
