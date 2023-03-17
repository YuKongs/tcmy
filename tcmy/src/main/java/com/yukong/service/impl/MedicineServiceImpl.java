package com.yukong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yukong.common.CustomException;
import com.yukong.dto.MedicineDto;
import com.yukong.entity.Medicine;
import com.yukong.mapper.MedicineMapper;
import com.yukong.service.MedicineSpecService;
import com.yukong.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MedicineServiceImpl extends ServiceImpl<MedicineMapper, Medicine> implements MedicineService {

    @Autowired
    private MedicineSpecService medicineSpecService;


    /**
     * 新增药品，同时保存对应的药品规格数据
     * @param medicineDto
     */
    @Override
    @Transactional
    public void saveWithSpec(MedicineDto medicineDto) {
        // 保存药品的基本信息到药品表medicine
        this.save(medicineDto);

        // 药品id
        Long medicineId = medicineDto.getId();

        // 药品药品规格
        List<com.yukong.entity.MedicineSpec> spec = medicineDto.getFlavors();
        spec.stream().map((item) -> {
            item.setMedicineId(medicineId);
            return item;
        }).collect(Collectors.toList());

        // 保存药品药品规格数据到药品药品规格表medicine_spec
        medicineSpecService.saveBatch(spec);
    }

    /**
     * 根据id查询药品信息和对应的药品规格信息
     * @param id
     * @return
     */
    @Override
    public MedicineDto getByIdWithSpec(Long id) {
        // 查询药品基本信息，从medicine表查询
        Medicine medicine = this.getById(id);

        // 对象拷贝
        MedicineDto medicineDto = new MedicineDto();
        BeanUtils.copyProperties(medicine,medicineDto);

        // 查询当前药品对应的药品规格信息，从medicine_spec表查询
        LambdaQueryWrapper<com.yukong.entity.MedicineSpec> lqw = new LambdaQueryWrapper<>();
        lqw.eq(com.yukong.entity.MedicineSpec::getMedicineId,medicine.getId());
        List<com.yukong.entity.MedicineSpec> spec = medicineSpecService.list(lqw);
        medicineDto.setFlavors(spec);

        return medicineDto;
    }

    /**
     * 更新药品信息，同时更新对应的药品规格信息
     * @param medicineDto
     */
    @Override
    @Transactional
    public void updateWithSpec(MedicineDto medicineDto) {
        // 更新medicine表基本信息
        this.updateById(medicineDto);

        // 清理当前药品对应的药品规格数据--medicine_spec表的delete操作
        LambdaQueryWrapper<com.yukong.entity.MedicineSpec> lqw = new LambdaQueryWrapper<>();
        lqw.eq(com.yukong.entity.MedicineSpec::getMedicineId,medicineDto.getId());
        medicineSpecService.remove(lqw);

        // 添加当前提交过来的药品规格数据--medicine_spec表的insert操作
        List<com.yukong.entity.MedicineSpec> spec = medicineDto.getFlavors();

        spec = spec.stream().map((item) -> {
            // 为药品规格设置当前绑定药品的id
            item.setMedicineId(medicineDto.getId());
            return item;
        }).collect(Collectors.toList());

        medicineSpecService.saveBatch(spec);
    }

    /**
     * 删除停售的药品信息,以及该药品的药品规格信息
     * 操作的表：medicine medicine_spec
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithSpec(List<Long> ids) {
        // 查询药品状态是否停售
        LambdaQueryWrapper<Medicine> lqw = new LambdaQueryWrapper<>();
        lqw.in(Medicine::getId,ids).eq(Medicine::getStatus,1);
        int count = this.count(lqw);
        if(count > 0){
            // 如果不能删除抛出一个业务异常
            throw new CustomException("药品正在售卖中，不能删除");
        }

        // 删除药品信息---medicine
        this.removeByIds(ids);

        // 删除药品药品规格信息---medicine_spec
        LambdaQueryWrapper<com.yukong.entity.MedicineSpec> medicineSpecLQW = new LambdaQueryWrapper<>();
        medicineSpecLQW.in(com.yukong.entity.MedicineSpec::getMedicineId,ids);
        // 执行删除 SQL：delete from medicine_spec where medicine_id in 1,2,3
        medicineSpecService.remove(medicineSpecLQW);

    }
}
