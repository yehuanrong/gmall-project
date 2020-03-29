package com.yhr.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.yhr.bean.UserInfo;
import com.yhr.bean.UserAddress;
import com.yhr.gmall.user.mapper.UserAddressMapper;
import com.yhr.gmall.user.mapper.UserMapper;
import com.yhr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> findAll() {
        return userMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {

        UserAddress userAddress=new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }
}
