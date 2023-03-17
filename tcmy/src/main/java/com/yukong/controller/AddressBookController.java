package com.yukong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yukong.common.BaseContext;
import com.yukong.common.R;
import com.yukong.entity.AddressBook;
import com.yukong.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理
 */
@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook: {}",addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @Transactional
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        log.info("addressBook: {}",addressBook);
        // 更新条件构造器
        LambdaUpdateWrapper<AddressBook> luw = new LambdaUpdateWrapper<>();
        // 添加条件
        luw.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        // 设置当前用户的所有地址为非默认地址
        luw.set(AddressBook::getIsDefault,0);
        // 执行更新 SQL:update address_book set is_default = 0 where user_id = ?
        addressBookService.update(luw);

        // 设置当前地址为默认地址
        addressBook.setIsDefault(1);
        // 执行更新 SQL: update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 查询地址
     * 编程默认规范：失败放在条件判断里面，成功放在条件判断外面，防止....
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);
        if(addressBook == null){
            return R.error("没有找到该对象");
        }
        return R.success(addressBook);
    }

    /**
     * 查询默认地址
     * 没有使用到 -> 在提交订单的时候用到了
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        // 条件构造器
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        // 设置查询的当前用户id
        lqw.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        // 设置查询的地址
        lqw.eq(AddressBook::getIsDefault,1);

        // 执行查询 SQL: select is_default from address_book where user_id = ? and is_default = ?
        AddressBook addressBook = addressBookService.getOne(lqw);
        if (addressBook == null){
            return R.error("没有找到该对象");
        }
        return R.success(addressBook);
    }

    /**
     * 查询指定用户的全部地址
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}",addressBook);

        // 条件构造器
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(null != addressBook.getUserId(),AddressBook::getUserId,addressBook.getUserId());
        lqw.orderByDesc(AddressBook::getUpdateTime);
         // 执行查询 SQL: select * from address_book where user_id = ? order by update_time desc;
        List<AddressBook> list = addressBookService.list(lqw);
        return R.success(list);
    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<AddressBook> update(@RequestBody AddressBook addressBook){
        log.info("addressBook: {}",addressBook);

        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 删除地址
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam Long ids){
        log.info("ids: {}", ids);
        addressBookService.removeById(ids);
        return R.success("删除成功");
    }

}
