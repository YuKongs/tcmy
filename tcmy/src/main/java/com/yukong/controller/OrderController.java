package com.yukong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yukong.common.BaseContext;
import com.yukong.common.R;
import com.yukong.dto.OrdersDto;
import com.yukong.entity.OrderDetail;
import com.yukong.entity.Orders;
import com.yukong.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 订单
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据:{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, @RequestParam(required = false) Long number, @RequestParam(required = false) String beginTime,@RequestParam(required = false) String endTime){
        log.info("page: {},pageSize: {},number: {},beginTime: {}, endTime: {}",page,pageSize,number,beginTime,endTime);
        // 分页构造器
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        // 条件构造器
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(number != null,Orders::getNumber,number);
        // 开始和结束时间
        lqw.between(beginTime != null,Orders::getCheckoutTime,beginTime,endTime);

        orderService.page(pageInfo,lqw);
        return R.success(pageInfo);
    }

    /**
     * 分页查询订单以及订单详情信息
     * 移动端调用
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize){
        log.info("page:{},pageSize{}",page,pageSize);
        // 获取当前登录用户的id
        Long currentId = BaseContext.getCurrentId();
        Page<OrdersDto> pageInfo = orderService.getOrderAndOrderDetailPage(page, pageSize, currentId);
        return R.success(pageInfo);
    }

    /**
     * 订单派送
     * @param orders
     * @return
     */
    @PutMapping()
    public R<String> deliver(@RequestBody Orders orders){
        log.info(orders.toString());
        LambdaUpdateWrapper<Orders> luw = new LambdaUpdateWrapper<>();
        luw.set(Orders::getStatus,orders.getStatus()).eq(Orders::getId,orders.getId());
        orderService.update(luw);
        return R.success("派送成功");
    }
}
