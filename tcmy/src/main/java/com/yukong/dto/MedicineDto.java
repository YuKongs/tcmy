package com.yukong.dto;


import com.yukong.entity.Medicine;
import com.yukong.entity.MedicineSpec;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MedicineDto extends Medicine {

    // 药品对应的药品规格数据数据
    private List<MedicineSpec> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
