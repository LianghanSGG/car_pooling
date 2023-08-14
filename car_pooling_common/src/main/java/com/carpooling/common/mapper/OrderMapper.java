package com.carpooling.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carpooling.common.pojo.db.Order;
import com.carpooling.common.pojo.vo.OderListConditionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author LiangHanSggg
 * @date 2023-08-03 14:14
 */
public interface OrderMapper extends BaseMapper<Order> {

    List<Order> conditionList(@Param("condition") OderListConditionVO condition);

}
