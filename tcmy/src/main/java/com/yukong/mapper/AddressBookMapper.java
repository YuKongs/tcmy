package com.yukong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yukong.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
