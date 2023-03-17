package com.yukong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yukong.common.R;
import com.yukong.entity.Category;
import com.yukong.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增药品分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category: {}",category);
        categoryService.save(category);
        return R.success("新增分类成功！");
    }

    /**
     * 分页条件查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        log.info("page: {},pageSize: {}",page,pageSize);

        // 构建分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        // 构建条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        // 添加排序条件,根据sort进行排序
        lqw.orderByAsc(Category::getSort);
        // 执行查询
        categoryService.page(pageInfo,lqw);

        return R.success(pageInfo);
    }

    /**
     * 根据id删除分类
     * 注意：当前分类如果关联了药品时，此分类不允许删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除分类，id为: {}",ids);

        categoryService.remove(ids);

        return R.success("分类信息删除成功！");
    }

    /**
     * 更新操作，自动填充
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息：{}",category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    /**
     * 根据条件查询分类数据 （查询的是添加药品中的药品分类数据）
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        // 条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        // 添加条件
        lqw.eq(category.getType() != null,Category::getType,category.getType());
        // 添加排序条件
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        // 执行查询
        List<Category> list = categoryService.list(lqw);
        return R.success(list);

    }
}
