package com.carpooling.common.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author LiangHanSggg
 * @date 2023-08-15 14:15
 */
@Data
@Accessors(chain = true)
public class OrderDetailVO extends OrderInfoVO {

    /**
     * 订单状态 0拼单中，1已完成
     */
    Integer state;

    List<PassengerVO> passengerList;
}
