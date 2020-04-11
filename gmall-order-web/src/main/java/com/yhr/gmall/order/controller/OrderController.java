package com.yhr.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.CartInfo;
import com.yhr.bean.OrderDetail;
import com.yhr.bean.OrderInfo;
import com.yhr.bean.UserAddress;
import com.yhr.gmall.config.LoginRequire;
import com.yhr.service.CartService;
import com.yhr.service.OrderService;
import com.yhr.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

   /* @RequestMapping("/trade")
    public String trade(){

        return "index";
    }*/

   @RequestMapping("/trade")
   @LoginRequire
   public String trade(HttpServletRequest request){

       String userId = (String) request.getAttribute("userId");

       List<UserAddress> userAddressList = userService.getUserAddressList(userId);

       request.setAttribute("userAddressList",userAddressList);

       List<CartInfo> cartInfoList = cartService.getCartCheckedList(userId);

       //声明一个集合保存orderDetail
       List<OrderDetail> orderDetailList=new ArrayList<>();

       for (CartInfo cartInfo : cartInfoList) {

           OrderDetail orderDetail=new OrderDetail();

           orderDetail.setSkuId(cartInfo.getSkuId());
           orderDetail.setSkuName(cartInfo.getSkuName());
           orderDetail.setImgUrl(cartInfo.getImgUrl());
           orderDetail.setSkuNum(cartInfo.getSkuNum());
           orderDetail.setOrderPrice(cartInfo.getCartPrice());

           orderDetailList.add(orderDetail);
       }

       //计算总金额
       OrderInfo orderInfo=new OrderInfo();
       orderInfo.setOrderDetailList(orderDetailList);

       orderInfo.sumTotalAmount();

       //保存总金额
       request.setAttribute("totalAmount",orderInfo.getTotalAmount());

       request.setAttribute("orderDetailList",orderDetailList);

       String tradeNo = orderService.getTradeNo(userId);
       request.setAttribute("tradeNo",tradeNo);

       return "trade";
   }

   @RequestMapping("/submitOrder")
   @LoginRequire
   public String submitOrder(HttpServletRequest request,OrderInfo orderInfo){

       String userId = (String) request.getAttribute("userId");

       orderInfo.setUserId(userId);

        //判断是否重复提交，获取流水号
       String tradeNo = request.getParameter("tradeNo");

       boolean result = orderService.checkTradeCode(userId, tradeNo);

       if(!result){

           request.setAttribute("errMsg","该页面已失效，请重新结算!");
           return "tradeFail";
       }

       String orderId=orderService.saveOrder(orderInfo);

       //删除流水号
       orderService.delTradeCode(userId);


       //跳转到支付页面
       return "redirect://payment.gmall.com/index?orderId="+orderId;
   }

}
