package com.yukong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yukong.dto.MedicineDto;
import com.yukong.entity.Medicine;

import java.util.List;

public interface MedicineService extends IService<Medicine> {

    // 新增药品，同时插入药品对应的口味数据，需要操作两张表：medicine，medicine_spec
    void saveWithSpec(MedicineDto medicineDto);

    // 根据id查询药品信息和对应口味信息(数据回显)
    MedicineDto getByIdWithSpec(Long id);

    // 更新药品信息，同时更新对应的口味信息
    void updateWithSpec(MedicineDto medicineDto);

    // 删除停售的药品信息，同时删除关联的套餐中该药品的信息，以及该药品的口味信息
    void removeWithSpec(List<Long> ids);

}
