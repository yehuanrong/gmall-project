package com.yhr.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.CartInfo;
import com.yhr.bean.SkuInfo;
import com.yhr.gmall.config.LoginRequire;
import com.yhr.service.CartService;
import com.yhr.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Reference
    private ManagerService managerService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    //如何区分用户是否登录，只需要看userId
    @RequestMapping("/addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){

        //获取商品数量和skuId
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");

        String userId = (String) request.getAttribute("userId");

        if(userId!=null){
            //调用方法添加购物车
            cartService.addToCart(skuId,userId, Integer.parseInt(skuNum));
        }else {

            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }

        //根据skuId查询skuInfo
        SkuInfo skuInfo = managerService.getSkuInfo(skuId);

        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);

        return "success";
    }

    @RequestMapping("/cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfoList=null;
        if(userId!=null){

            //合并购物车
            List<CartInfo> cartListCk= cartCookieHandler.getCartList(request);
            if(cartListCk!=null && cartListCk.size()>0){

                //合并购物车
                cartInfoList=cartService.mergeToCartList(cartListCk,userId);

                //删除未登录购物车
                cartCookieHandler.deleteCartCookie(request,response);

            }else {

                //登录状态下查询
                cartInfoList= cartService.getCartList(userId);
            }

        }else {

            //未登录状态下查询
            cartInfoList= cartCookieHandler.getCartList(request);
        }

        request.setAttribute("cartInfoList",cartInfoList);

        return "cartList";
    }

    @RequestMapping("/checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(HttpServletRequest request,HttpServletResponse response){

        //获取页面传递的数据
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");

        String userId=(String) request.getAttribute("userId");

        if(userId!=null){

            cartService.checkCart(skuId,isChecked,userId);
        }else {

            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }

    }

    @RequestMapping("/toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request,HttpServletResponse response){

        //合并勾选的商品 登录加未登录的
        List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);

        String userId=(String) request.getAttribute("userId");

        if (cartListCK!=null && cartListCK.size()>0){
            cartService.mergeToCartList(cartListCK, userId);

            //删除未登录的数据
            cartCookieHandler.deleteCartCookie(request,response);
        }

        return "redirect://order.gmall.com/trade";

    }
}
