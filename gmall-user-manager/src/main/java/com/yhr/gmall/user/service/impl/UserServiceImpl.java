package com.yhr.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yhr.bean.UserAddress;
import com.yhr.bean.UserInfo;
import com.yhr.gmall.config.RedisUtil;
import com.yhr.gmall.user.mapper.UserAddressMapper;
import com.yhr.gmall.user.mapper.UserMapper;
import com.yhr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;


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

    @Override
    public UserInfo login(UserInfo userInfo) {

        String passwd = userInfo.getPasswd();
        //对密码进行加密
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPasswd);

        UserInfo info = userMapper.selectOne(userInfo);

        if(info!=null){

            Jedis jedis=redisUtil.getJedis();

            String userKey=userKey_prefix+info.getId()+userinfoKey_suffix;

            jedis.setex(userKey,userKey_timeOut, JSON.toJSONString(info));

            jedis.close();

            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {

        Jedis jedis=redisUtil.getJedis();

        String userKey=userKey_prefix+userId+userinfoKey_suffix;

        String userJson = jedis.get(userKey);

        if(!StringUtils.isEmpty(userJson)){

            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);

            jedis.close();
            return userInfo;
        }

        return null;
    }
}
