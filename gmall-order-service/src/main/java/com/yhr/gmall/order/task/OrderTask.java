package com.yhr.gmall.order.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.OrderInfo;
import com.yhr.service.OrderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class OrderTask {

    @Reference
    private OrderService orderService;

    @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder(){

        List<OrderInfo> expiredOrderList = orderService.getExpiredOrderList();

        for (OrderInfo orderInfo : expiredOrderList) {

            //关闭过期订单
            orderService.execExpiredOrder(orderInfo);
        }
    }
}
