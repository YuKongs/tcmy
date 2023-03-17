package com.yukong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yukong.common.BaseContext;
import com.yukong.common.R;
import com.yukong.entity.ShoppingCart;
import com.yukong.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("shoppingCart: {}",shoppingCart);
        // 设置用户id，指定当前是哪一个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前药品是否在购物车中
        Long medicineId = shoppingCart.getMedicineId();

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,currentId);

        if(medicineId != null){
            // 添加到购物车的是药品
            lqw.eq(ShoppingCart::getMedicineId,medicineId);
        }else{
            lqw.eq(ShoppingCart::getMedicineId,shoppingCart.getMedicineId());
        }

        // 查询当前药品是否在购物车中
        // SQL:select * from shopping_cart where user_id = ? and medicine_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(lqw);

        if (cartServiceOne != null){
            // 如果已经存在，就在原来数量基础上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            // 如果不存在，则添加到购物车，数量默认就是一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车..");
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        lqw.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(lqw);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> delete(){
        log.info("清空购物车...");
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        // 执行删除
        shoppingCartService.remove(lqw);
        return R.success("清空购物车成功！");
    }

    /**
     * 药品数量减一
     * 未完成
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(){
        log.info("减少药品数量");
        // 获取当前药品的数量
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        return null;
    }
}
