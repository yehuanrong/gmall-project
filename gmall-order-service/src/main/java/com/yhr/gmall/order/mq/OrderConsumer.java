package com.yhr.gmall.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.enums.ProcessStatus;
import com.yhr.service.OrderService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {

    @Reference
    private OrderService orderService;


    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public  void  consumerPaymentResult(MapMessage mapMessage) throws JMSException {

        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        if ("success".equals(result)){

            // 更新支付状态
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);

            // 通知减库存
            orderService.sendOrderStatus(orderId);
            orderService.updateOrderStatus(orderId, ProcessStatus.DELEVERED);

        }
    }

    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException{

        String orderId = mapMessage.getString("orderId");
        String  status = mapMessage.getString("status");
        if("DEDUCTED".equals(status)){
            orderService.updateOrderStatus(orderId , ProcessStatus.WAITING_DELEVER);
        }else{
            orderService.updateOrderStatus(orderId , ProcessStatus.STOCK_EXCEPTION);
        }

    }

}
