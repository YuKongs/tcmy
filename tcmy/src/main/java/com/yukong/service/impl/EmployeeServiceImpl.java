package com.yukong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yukong.entity.Employee;
import com.yukong.mapper.EmployeeMapper;
import com.yukong.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
