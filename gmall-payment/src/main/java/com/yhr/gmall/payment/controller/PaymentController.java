package com.yhr.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.yhr.bean.OrderInfo;
import com.yhr.bean.PaymentInfo;
import com.yhr.bean.enums.PaymentStatus;
import com.yhr.gmall.payment.config.AlipayConfig;
import com.yhr.service.OrderService;
import com.yhr.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;

    @Reference
    private PaymentService paymentService;

    @Autowired
    private AlipayClient alipayClient;

    @RequestMapping("/index")
    public String index(HttpServletRequest request,String orderId){

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        request.setAttribute("orderId",orderId);
        //获取总金额
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        return "index";
    }

    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String alipaySubmit(HttpServletRequest request, HttpServletResponse response){

        /**
         * 1.保存支付信息，作用：追踪交易状态、去重、对账 paymentInfo 放入数据库
         * 2.生成二维码
         */
        //通过orderId将数据查询出来
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        PaymentInfo paymentInfo=new PaymentInfo();
        //属性赋值
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("给yhr买");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());

        paymentService.savePaymentInfo(paymentInfo);

        //生成二维码
        AlipayTradePagePayRequest alipayRequest=new AlipayTradePagePayRequest();//创建api对应的request

        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);//同步回调
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
       /* alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");*/

        // 声明一个Map来存储参数
        HashMap<String,Object> bizContnetMap=new HashMap<>();

        bizContnetMap.put("out_trade_no",paymentInfo.getOutTradeNo());
        bizContnetMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        bizContnetMap.put("subject",paymentInfo.getSubject());
        bizContnetMap.put("total_amount",paymentInfo.getTotalAmount());

        alipayRequest.setBizContent(JSON.toJSONString(bizContnetMap));

        String form="";

        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单

        } catch (AlipayApiException e) {

            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");

        //调用延迟队列
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);

        return form;

    }

    //同步回调
    @RequestMapping("/alipay/callback/return")
    public String callbackReturn(){

        return "redirect:"+AlipayConfig.return_order_url;
    }

    //异步回调
    @RequestMapping("/alipay/callback/notify")
    @ResponseBody
    public String callbackNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request){

        boolean flag=false;

        try {
            flag= AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key,
                    "utf-8",AlipayConfig.sign_type);
        } catch (AlipayApiException e) {

            e.printStackTrace();
        }

        if(flag){
            //需要得到trade_status
            String trade_status = paramMap.get("trade_status");

            //通过out_trade_no查询支付状态
            String out_trade_no = paramMap.get("out_trade_no");

            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){

                PaymentInfo paymentInfo = new PaymentInfo();

                paymentInfo.setOutTradeNo(out_trade_no);

                PaymentInfo paymentInfoHas = paymentService.getPaymentInfo(paymentInfo);

                if(paymentInfoHas.getPaymentStatus()==PaymentStatus.PAID || paymentInfoHas.getPaymentStatus()==PaymentStatus.ClOSED){

                    return "failure";
                }

                // 修改
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                // 设置状态
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                // 设置创建时间
                paymentInfoUpd.setCallbackTime(new Date());

                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);

                //发送消息队列给订单：orderId,result
                paymentService.sendPaymentResult(paymentInfo,"success");

                return "success";
            }

        }else{

            return "failure";
        }

        return "failure";

    }

    //退款
    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId){

        boolean flag = paymentService.refund(orderId);

        return ""+flag;
    }

    @RequestMapping("wx/submit")
    @ResponseBody
    public Map createNative(String orderId){

        //  // 做一个判断：支付日志中的订单支付状态 如果是已支付，则不生成二维码直接重定向到消息提示页面！
        // 调用服务层数据
        // 第一个参数是订单Id ，第二个参数是多少钱，单位是分
        Map map = paymentService.createNative(orderId,"1");

        return map;

    }

    // 发送验证
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,@RequestParam("result") String result){

        paymentService.sendPaymentResult(paymentInfo,result);

        return "sent payment result";
    }


    // 查询订单信息
    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(HttpServletRequest request){

        String orderId = request.getParameter("orderId");

        PaymentInfo paymentInfoQuery = new PaymentInfo();

        paymentInfoQuery.setOrderId(orderId);

        boolean flag = paymentService.checkPayment(paymentInfoQuery);

        return ""+flag;
    }


}
