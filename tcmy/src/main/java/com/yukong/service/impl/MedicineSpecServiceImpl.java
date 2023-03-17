package com.yukong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yukong.mapper.MedicineSpecMapper;
import com.yukong.service.MedicineSpecService;
import org.springframework.stereotype.Service;

@Service
public class MedicineSpecServiceImpl extends ServiceImpl<MedicineSpecMapper, com.yukong.entity.MedicineSpec> implements MedicineSpecService {
}
