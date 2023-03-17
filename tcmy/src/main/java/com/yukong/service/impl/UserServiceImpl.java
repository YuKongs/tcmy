package com.yukong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yukong.entity.User;
import com.yukong.mapper.UserMapper;
import com.yukong.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
