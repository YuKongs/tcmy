package com.yukong.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车
 */
@Data
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //名称
    private String name;

    //用户id
    private Long userId;

    //药品id
    private Long medicineId;

    //药品规格
    private String medicineSpec;

    //数量
    private Integer number;

    //金额
    private BigDecimal amount;

    //图片
    private String image;

//    @TableField(fill = FieldFill.INSERT)  这里加注解会报错
    private LocalDateTime createTime;
}
