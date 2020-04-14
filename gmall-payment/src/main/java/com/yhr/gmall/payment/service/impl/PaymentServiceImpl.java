package com.yhr.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.github.wxpay.sdk.WXPayUtil;
import com.yhr.bean.OrderInfo;
import com.yhr.bean.PaymentInfo;
import com.yhr.bean.enums.PaymentStatus;
import com.yhr.gmall.config.ActiveMQUtil;
import com.yhr.gmall.payment.mapper.PaymentInfoMapper;
import com.yhr.gmall.util.HttpClient;
import com.yhr.service.OrderService;
import com.yhr.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private AlipayClient alipayClient;

    @Reference
    private OrderService orderService;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    // 服务号Id
    @Value("${appid}")
    private String appid;
    // 商户号Id
    @Value("${partner}")
    private String partner;
    // 密钥
    @Value("${partnerkey}")
    private String partnerkey;


    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {

        return paymentInfoMapper.selectOne(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd) {

        Example example=new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);

        paymentInfoMapper.updateByExampleSelective(paymentInfoUpd,example);

    }

    @Override
    public boolean refund(String orderId) {

        //通过orderId获取数据
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();//创建api对应的request

        HashMap<String, Object> map = new HashMap<>();

        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("refund_amount", orderInfo.getTotalAmount());

        request.setBizContent(JSON.toJSONString(map));

        AlipayTradeRefundResponse response = null;

        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){

            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }

    }

    @Override
    public Map createNative(String orderId, String total_fee) {

        //1.创建参数
        Map<String,String> param=new HashMap();//创建参数

        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "尚硅谷");//商品描述
        param.put("out_trade_no", orderId);//商户订单号
        param.put("total_fee",total_fee);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://order.gmall.com/trade");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型

        try {

            //2.生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);

            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");

            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            //3.获得结果
            String result = client.getContent();

            //将结果转换为map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, String> map=new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", total_fee);//总金额
            map.put("out_trade_no",orderId);//订单号

            return map;

        }catch (Exception e){

            e.printStackTrace();

            return new HashMap<>();
        }

    }

    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {

        Connection connection = activeMQUtil.getConnection();

        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_QUEUE");

            MessageProducer producer = session.createProducer(paymentResultQueue);

            MapMessage mapMessage = new ActiveMQMapMessage();

            mapMessage.setString("orderId",paymentInfo.getOrderId());
            mapMessage.setString("result",result);

            producer.send(mapMessage);

            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {

        Connection connection = activeMQUtil.getConnection();

        try {
            connection.start();

            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");

            MessageProducer producer = session.createProducer(paymentResultQueue);

            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setInt("delaySec",delaySec);
            mapMessage.setInt("checkCount",checkCount);

            // 设置延迟多少时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);

            producer.send(mapMessage);

            session.commit();
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }



    }

    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {
        // 查询当前的支付信息
        PaymentInfo paymentInfo = getPaymentInfo(paymentInfoQuery);

        if (paymentInfo.getPaymentStatus()== PaymentStatus.PAID || paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
            return true;
        }
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\""+paymentInfo.getOutTradeNo()+"\"" +
                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            if ("TRADE_SUCCESS".equals(response.getTradeStatus())||"TRADE_FINISHED".equals(response.getTradeStatus())){
                //  IPAD
                // 改支付状态
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfo.getOutTradeNo(),paymentInfoUpd);
                sendPaymentResult(paymentInfo,"success");
                return true;
            }else {

                return false;
            }

        } else {

            return false;
        }

    }

    @Override
    public void closePayment(String orderId) {

        /**
         * updateByExampleSelective中，第一个参数表示要更新的值
         * 第二个参数表示按照什么条件更新
         */

        Example example = new Example(PaymentInfo.class);

        example.createCriteria().andEqualTo("orderId",orderId);

        PaymentInfo paymentInfo = new PaymentInfo();

        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);

        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);

    }
}
