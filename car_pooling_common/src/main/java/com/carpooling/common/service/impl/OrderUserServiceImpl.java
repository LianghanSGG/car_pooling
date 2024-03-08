package com.carpooling.common.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.exception.DException;
import com.carpooling.common.mapper.OrderUserMapper;
import com.carpooling.common.pojo.db.Order;
import com.carpooling.common.pojo.db.OrderUser;
import com.carpooling.common.pojo.vo.OrderDetailVO;
import com.carpooling.common.pojo.vo.PassengerVO;
import com.carpooling.common.service.OrderService;
import com.carpooling.common.service.OrderUserService;
import com.carpooling.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LiangHanSggg
 * @date 2023-07-25 19:53
 */
@Service
public class OrderUserServiceImpl extends ServiceImpl<OrderUserMapper, OrderUser> implements OrderUserService {

    @Autowired
    private OrderService orderService;

    static DateTimeFormatter time_formatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public List<OrderUser> getHistory(int index, int size) {
        Long userId = UserContext.get().getId();
        Page<OrderUser> p = new Page<>(index, size);
        page(p, Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getUserId, userId)
                .ne(OrderUser::getState, 0).orderByDesc(OrderUser::getAppointmentTime));
        return p.getRecords();
    }

    @Override
    public OrderDetailVO getHistoryDetail(Long orderId) {
        Long userId = UserContext.get().getId();
        LambdaQueryWrapper<Order> eq1 = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getId, orderId);

        Order order = orderService.getOne(eq1);
        if (order == null) throw new DException("订单不存在");

        OrderDetailVO orderDetail = getOrderDetail(order);

        List<OrderUser> passenger = list(Wrappers.lambdaQuery(OrderUser.class)
                .eq(OrderUser::getOrderId, order.getId())
                .select(OrderUser::getUserId, OrderUser::getUserSex, OrderUser::getUserName, OrderUser::getUserPhone, OrderUser::getUserWechatAccount, OrderUser::getUserRole));

        if (passenger == null || passenger.isEmpty()) return orderDetail;
        List<PassengerVO> passengerVOS = new ArrayList<>();

        for (OrderUser orderUser : passenger) {
            PassengerVO passengerVO = new PassengerVO();
            boolean flag = false;
            // 除了用户自身和团长，剩下的都脱敏
            if (orderUser.getUserId().equals(userId) || (flag = (orderUser.getUserRole() == 0))) {
                passengerVO.setUserId(orderUser.getUserId())
                        .setUserName(orderUser.getUserName())
                        .setSex(orderUser.getUserSex())
                        .setPhone(orderUser.getUserPhone())
                        .setWxAccount(orderUser.getUserWechatAccount());
                if (flag) {
                    passengerVO.setOwner(1);
                }

            } else {
                passengerVO.setUserName(orderUser.getUserName().substring(0, 1) + "同学")
                        .setPhone(DesensitizedUtil.mobilePhone(orderUser.getUserPhone()))
                        .setSex(orderUser.getUserSex());
            }
            passengerVOS.add(passengerVO);
        }

        orderDetail.setPassengerList(passengerVOS);

        return orderDetail;
    }

    /**
     * 创建OrderDetailVO 只创建VO中的订单部分，不包含乘客
     *
     * @param order
     * @return
     */
    public OrderDetailVO getOrderDetail(Order order) {

        OrderDetailVO orderDetailVO = new OrderDetailVO();
        orderDetailVO.setOrderId(order.getId())
                .setStartPlace(order.getStartPlace())
                .setEndPlace(order.getEndPlace())
                .setAppointmentTime(order.getAppointmentTime().toString())
                .setEarliestTime(time_formatter.format(order.getEarliestTime()))
                .setLatestTime(time_formatter.format(order.getLatestTime()))
                .setAlreadyNumber(order.getAlreadyNumber())
                .setTargetNumber(order.getTargetNumber())
                .setSex(order.getSex())
                .setAutoJoin(order.getAutoJoin());
        orderDetailVO.setState(order.getState());

        return orderDetailVO;

    }
}
