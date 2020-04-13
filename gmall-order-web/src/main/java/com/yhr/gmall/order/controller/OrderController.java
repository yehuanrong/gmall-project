package com.yhr.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.*;
import com.yhr.gmall.config.LoginRequire;
import com.yhr.service.CartService;
import com.yhr.service.ManagerService;
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

    @Reference
    private ManagerService managerService;

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

       //验证库存
       List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

       for (OrderDetail orderDetail : orderDetailList){

           boolean flag = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
           if (!flag) {
               request.setAttribute("errMsg", orderDetail.getSkuName()+"商品库存不足，请重新下单！");

               return "tradeFail";
             }

             //验证价格
           SkuInfo skuInfo = managerService.getSkuInfo(orderDetail.getSkuId());

           int res = skuInfo.getPrice().compareTo(orderDetail.getOrderPrice());

           if(res!=0){

               request.setAttribute("errMsg", orderDetail.getSkuName()+"价格不匹配！");

               cartService.loadCartCache(userId);

               return "tradeFail";
           }

       }

           String orderId=orderService.saveOrder(orderInfo);

       //删除流水号
       orderService.delTradeCode(userId);


       //跳转到支付页面
       return "redirect://payment.gmall.com/index?orderId="+orderId;
   }

}
