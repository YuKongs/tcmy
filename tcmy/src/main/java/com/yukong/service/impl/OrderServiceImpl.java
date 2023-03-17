package com.yukong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yukong.common.BaseContext;
import com.yukong.common.CustomException;
import com.yukong.common.R;
import com.yukong.dto.OrdersDto;
import com.yukong.entity.*;
import com.yukong.mapper.OrderMapper;
import com.yukong.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;
    /**
     * 用户下单
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        // 获得当前用户的id
        Long userID = BaseContext.getCurrentId();

        // 查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userID);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lqw);

        if (shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomException("购物车为空，不能下单");
        }
        // 查询用户数据
        User user = userService.getById(userID);

        // 查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId();  // 订单号

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) ->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setMedicineSpec(item.getMedicineSpec());
            orderDetail.setMedicineId(item.getMedicineId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userID);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        // 向订单表插入数据,一条数据
        this.save(orders);

        // 向订单明细表插入数据
        orderDetailService.saveBatch(orderDetails);

        // 清空购物车数据
        shoppingCartService.remove(lqw);
    }

    /**
     * 分页查询订单以及订单详情信息
     * 移动端调用
     * @param page
     * @param pageSize
     * @param userId
     * @return
     */
    @Override
    public Page<OrdersDto> getOrderAndOrderDetailPage(int page, int pageSize, Long userId) {
        // 构建分页构造器
        Page<Orders> ordersPage = new Page<>();
        Page<OrdersDto> ordersDtoPage = new Page<>();

        // 1.获取订单信息
        LambdaQueryWrapper<Orders> ordersLQW = new LambdaQueryWrapper<>();
        ordersLQW.eq(Orders::getUserId,userId);
        ordersLQW.orderByDesc(Orders::getOrderTime);
        this.page(ordersPage,ordersLQW);
        // 2.对象拷贝
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        List<Orders> records = ordersPage.getRecords();
        List<OrdersDto> list = records.stream().map((item) ->{
            // 2.1 拷贝订单基础信息
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item,ordersDto);
            // 2.2 获取订单详情信息
            LambdaQueryWrapper<OrderDetail> lqw = new LambdaQueryWrapper<>();
            lqw.eq(OrderDetail::getOrderId,item.getId());
            // 这里是否需要排序
            List<OrderDetail> orderDetailsList = orderDetailService.list(lqw);
            if(orderDetailsList != null){
                ordersDto.setOrderDetails(orderDetailsList);
            }
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(list);

        return ordersDtoPage;
    }
}
