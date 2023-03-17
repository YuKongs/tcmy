package com.yukong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yukong.entity.Category;

public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
