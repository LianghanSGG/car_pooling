package com.carpooling.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carpooling.common.pojo.db.Complain;
import com.carpooling.common.pojo.vo.ComplainVO;

/**
 * @author LiangHanSggg
 * @date 2023-07-25 16:05
 */
public interface ComplainService extends IService<Complain> {

    boolean addComplain(boolean exist, ComplainVO complainVO);

    String getReplay(Long complainId);
}
