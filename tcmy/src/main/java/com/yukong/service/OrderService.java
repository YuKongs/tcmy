package com.yukong.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yukong.common.R;
import com.yukong.dto.OrdersDto;
import com.yukong.entity.Orders;

public interface OrderService extends IService<Orders> {
    // 用户下单
    void submit(Orders orders);

    // 分页查询订单以及订单详细信息
    Page<OrdersDto> getOrderAndOrderDetailPage(int page, int pageSize, Long userId);
}
