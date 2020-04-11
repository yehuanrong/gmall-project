package com.yhr.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.yhr.bean.OrderDetail;
import com.yhr.bean.OrderInfo;
import com.yhr.bean.enums.OrderStatus;
import com.yhr.bean.enums.ProcessStatus;
import com.yhr.gmall.config.RedisUtil;
import com.yhr.gmall.order.mapper.OrderDetailMapper;
import com.yhr.gmall.order.mapper.OrderInfoMapper;
import com.yhr.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {

        //数据结构不完整
        // 生成第三方支付编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);

        // 设置创建时间
        orderInfo.setCreateTime(new Date());
        // 设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());

        orderInfo.setOrderStatus(OrderStatus.UNPAID);

        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        orderInfo.sumTotalAmount();

        orderInfoMapper.insertSelective(orderInfo);

        //订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {

            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }

        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {

        Jedis jedis = redisUtil.getJedis();

        String tradeNoKey="user:"+userId+":tradeCode";

        String tradeCode = UUID.randomUUID().toString();

        jedis.set(tradeNoKey,tradeCode);

        jedis.close();

        return tradeCode;

    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {

        Jedis jedis=redisUtil.getJedis();

        String tradeNoKey="user:"+userId+":tradeCode";

        String tradeNo = jedis.get(tradeNoKey);

        jedis.close();

        return tradeCodeNo.equals(tradeNo);
    }

    @Override
    public void delTradeCode(String userId) {

        Jedis jedis = redisUtil.getJedis();

        String tradeNoKey =  "user:"+userId+":tradeCode";

        jedis.del(tradeNoKey);

        jedis.close();

    }
}
