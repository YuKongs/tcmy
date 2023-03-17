package com.yukong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yukong.common.CustomException;
import com.yukong.entity.Category;
import com.yukong.entity.Medicine;
import com.yukong.mapper.CategoryMapper;
import com.yukong.service.CategoryService;
import com.yukong.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
   @Autowired
   private MedicineService medicineService;

    /**
     * 根据id删除分类，删除之前进行判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 1.查询当前分类是否关联了药品，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Medicine> medicineLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据分类id查询
        medicineLambdaQueryWrapper.eq(Medicine::getCategoryId,id);
        int medicineCount = medicineService.count(medicineLambdaQueryWrapper);

        if (medicineCount > 0){
            // 已经关联药品，抛出一个业务异常
            throw new CustomException("当前分类下关联了药品，不能删除");
        }

        // 2.正常删除分类
        super.removeById(id);
    }
}
