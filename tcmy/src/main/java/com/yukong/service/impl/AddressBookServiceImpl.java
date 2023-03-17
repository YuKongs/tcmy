package com.yukong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yukong.entity.AddressBook;
import com.yukong.mapper.AddressBookMapper;
import com.yukong.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
