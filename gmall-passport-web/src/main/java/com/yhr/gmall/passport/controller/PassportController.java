package com.yhr.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.UserInfo;
import com.yhr.gmall.passport.config.JwtUtil;
import com.yhr.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Value("${token.key}")
    private String key;

    @Reference
    private UserService userService;

    @RequestMapping("/index")
    public String index(HttpServletRequest request){

        String originUrl = request.getParameter("originUrl");

        //保存originUrl
        request.setAttribute("originUrl",originUrl);

        return "index";
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){

        //获取服务器的ip地址
        String salt = request.getHeader("X-forwarded-for");

        UserInfo info=userService.login(userInfo);

        if(info!=null){

            //登录成功之后返回token！

            HashMap<String,Object> map=new HashMap<>();
            map.put("userId",info.getId());
            map.put("nickName",info.getNickName());

            String token = JwtUtil.encode(key, map, salt);

            return token;
        }else{

            return "fail";
        }

    }

    @RequestMapping("/verify")
    @ResponseBody
    public String verify(HttpServletRequest request){

       /* 用户登录的认证：
        1.	获取服务器的Ip，token
        2.	key+ip ,解密token 得到用户的信息 userId,nickName
        3.	判断用户是否登录：key=user:userId:info  value=userInfo
        4.	userInfo!=null true; false;*/

        String salt = request.getHeader("X-forwarded-for");
        //String salt = request.getParameter("salt");

        String token = request.getParameter("token");

        Map<String, Object> map = JwtUtil.decode(token, key, salt);

        if(map!=null && map.size()>0){

            String userId =(String)map.get("userId");

            UserInfo userInfo=userService.verify(userId);

            if(userInfo!=null){

                return "success";
            }else {

                return "fail";
            }
        }

        return "fail";

    }

}
