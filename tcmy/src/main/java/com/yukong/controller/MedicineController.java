package com.yukong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yukong.common.R;
import com.yukong.dto.MedicineDto;
import com.yukong.entity.Category;
import com.yukong.entity.Medicine;
import com.yukong.service.CategoryService;
import com.yukong.service.MedicineSpecService;
import com.yukong.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/medicine")
public class MedicineController {
    @Autowired
    private MedicineService medicineService;
    @Autowired
    private MedicineSpecService medicineSpecService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增药品
     * @param medicineDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody MedicineDto medicineDto){
        log.info(medicineDto.toString());
        medicineService.saveWithSpec(medicineDto);

        // 清理某个分类下面的药品缓存数据
        String key = "medicine_" + medicineDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("新增药品成功");
    }

    /**
     * 分页条件查询药品信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        // 创建分页构造器
        Page<Medicine> pageInfo = new Page<>(page,pageSize);
        Page<MedicineDto> medicineDtoPage = new Page<>();

        // 条件构造器
        LambdaQueryWrapper<Medicine> lqw = new LambdaQueryWrapper<>();
        // 添加过滤条件
        lqw.like(name != null, Medicine::getName,name);
        // 添加排序条件
        lqw.orderByDesc(Medicine::getUpdateTime);

        // 执行查询
        medicineService.page(pageInfo,lqw);

        // 对象拷贝
        /**
         * pageInfo 里面 List集合的泛型是Medicine，需要将泛型修改为MedicineDto
         */
        BeanUtils.copyProperties(pageInfo,medicineDtoPage,"records");
        List<Medicine> records = pageInfo.getRecords();
        List<MedicineDto> list = records.stream().map((item) -> {
            MedicineDto medicineDto = new MedicineDto();

            BeanUtils.copyProperties(item,medicineDto);

            Long categoryId = item.getCategoryId();  // 分类id
            // 根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            // 部分初始数据没有分类
            if (category != null){
                String categoryName = category.getName();
                medicineDto.setCategoryName(categoryName);
            }
            return medicineDto;
        }).collect(Collectors.toList());

        medicineDtoPage.setRecords(list);

        return R.success(medicineDtoPage);
    }

    /**
     * 根据id查询药品信息和对应口味信息(数据回显)
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<MedicineDto> get(@PathVariable Long id){
        MedicineDto medicineDto = medicineService.getByIdWithSpec(id);
        return R.success(medicineDto);
    }

    /**
     * 修改药品
     * @param medicineDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody MedicineDto medicineDto){
        log.info(medicineDto.toString());

        medicineService.updateWithSpec(medicineDto);

        /* 清理所有药品的缓存数据
        Set keys = redisTemplate.keys("medicine_*");
        redisTemplate.delete(keys);*/

        // 清理某个分类下面的药品缓存数据
        String key = "medicine_" + medicineDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("修改药品成功");
    }

    /**
     * 根据条件查询对应的药品数据
     * @param medicine
     * @return
     */

    @GetMapping("/list")
    public R<List<MedicineDto>> list(Medicine medicine){

        List<MedicineDto> list = null;

        // 动态构造Redis的key
        String key = "medicine_" + medicine.getCategoryId() + "_" +medicine.getStatus();// medicine_1213123131_1

        // 先从redis中获取缓存数据
        list = (List<MedicineDto>) redisTemplate.opsForValue().get(key);
        if (list != null){
            // 如果存在，直接返回，无需查询数据库
            return R.success(list);
        }

        // 构造查询条件对象
        LambdaQueryWrapper<Medicine> lqw = new LambdaQueryWrapper<>();
        lqw.eq(medicine.getCategoryId() != null, Medicine::getCategoryId,medicine.getCategoryId());
        // 添加条件，查询状态为1（起售状态）的药品
        lqw.eq(Medicine::getStatus,1);
        // 添加排序条件
        lqw.orderByAsc(Medicine::getSort).orderByDesc(Medicine::getUpdateTime);
        List<Medicine> medicines = medicineService.list(lqw);

        list = medicines.stream().map((item) -> {
            MedicineDto medicineDto = new MedicineDto();
            BeanUtils.copyProperties(item,medicineDto);

            // 当前药品的id
            Long medicineId = item.getId();
            LambdaQueryWrapper<com.yukong.entity.MedicineSpec> specLQW = new LambdaQueryWrapper<>();
            specLQW.eq(com.yukong.entity.MedicineSpec::getMedicineId,medicineId);

            // 执行查询 SQL：select * from medicine_spec where medicine_id = ?
            List<com.yukong.entity.MedicineSpec> medicineSpecList = medicineSpecService.list(specLQW);
            medicineDto.setFlavors(medicineSpecList);
            return medicineDto;
        }).collect(Collectors.toList());
        // 如果不存在，需要查询数据库，将查询到的药品数据缓存到Redis
        redisTemplate.opsForValue().set(key,list,60, TimeUnit.MINUTES);
        return R.success(list);
    }

    /**
     * 删除药品信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids: {}", ids);
        medicineService.removeWithSpec(ids);

        // 清理所有药品的缓存数据
        Set keys = redisTemplate.keys("medicine_*");
        redisTemplate.delete(keys);

        return R.success("药品信息删除成功");
    }

    /**
     * 更改售卖状态
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> onSale(@PathVariable int status,@RequestParam List<Long> ids){
        log.info("status: {}, ids: {}",status,ids);

        LambdaUpdateWrapper<Medicine> luw = new LambdaUpdateWrapper<>();
        luw.set(Medicine::getStatus,status).in(Medicine::getId,ids);

        medicineService.update(luw);

        // 清理所有药品的缓存数据
        Set keys = redisTemplate.keys("medicine_*");
        redisTemplate.delete(keys);

        return R.success("药品状态修改成功");
    }
}
