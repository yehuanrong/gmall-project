package com.yhr.service;

import com.yhr.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {

    /**
     * 保存交易记录
     * @param paymentInfo
     */
    void  savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 根据out_trade_no查询支付状态
     * @param paymentInfo
     * @return
     */

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 根据out_trade_no更新支付状态
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    //退款
    boolean refund(String orderId);

    //微信支付
    Map createNative(String orderId, String total_fee);

    /**
     * 发送消息队列给订单
     * @param paymentInfo
     * @param result
     */
    void sendPaymentResult(PaymentInfo paymentInfo,String result);

    /**
     * 发送延迟队列
     * @param outTradeNo
     * @param delaySec
     * @param checkCount
     */
    void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);

    //查询订单状态
    boolean checkPayment(PaymentInfo paymentInfoQuery);

    /**
     * 关闭交易记录
      * @param orderId
     */
    void closePayment(String orderId);
}
