package com.yukong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yukong.entity.ShoppingCart;
import com.yukong.mapper.ShoppingCartMapper;
import com.yukong.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
