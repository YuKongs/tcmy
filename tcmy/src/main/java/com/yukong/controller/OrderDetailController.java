package com.yukong.controller;

import com.yukong.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/orderDetail")
@RestController
public class OrderDetailController {
    @Autowired
    private OrderDetailService orderDetailService;
}
