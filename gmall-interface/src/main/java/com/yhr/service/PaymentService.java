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
}
