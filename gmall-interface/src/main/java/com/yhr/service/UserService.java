package com.yhr.service;

import com.yhr.bean.UserInfo;
import com.yhr.bean.UserAddress;

import java.util.List;

public interface UserService {

    /**
     * 查询所有数据
     * @return
     */

    List<UserInfo> findAll();

    /**
     * 根据userid查询user地址
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressList(String userId);
}
