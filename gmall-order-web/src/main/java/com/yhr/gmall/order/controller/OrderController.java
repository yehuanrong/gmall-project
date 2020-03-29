package com.yhr.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.UserAddress;
import com.yhr.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {

    @Reference
    private UserService userService;

   /* @RequestMapping("/trade")
    public String trade(){

        return "index";
    }*/

   @RequestMapping("/trade")
   @ResponseBody
   public List<UserAddress> trade(String userId){

       return userService.getUserAddressList(userId);
   }
}
