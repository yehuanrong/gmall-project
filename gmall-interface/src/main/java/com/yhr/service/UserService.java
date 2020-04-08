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

    /**
     * 登录方法
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 根据用户id查询数据（在redis中)
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}
