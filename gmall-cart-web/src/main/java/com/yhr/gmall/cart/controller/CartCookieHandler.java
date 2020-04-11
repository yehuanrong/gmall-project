package com.yhr.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.yhr.bean.CartInfo;
import com.yhr.bean.SkuInfo;
import com.yhr.gmall.config.CookieUtil;
import com.yhr.service.ManagerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {

    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    @Reference
    private ManagerService managerService;


    /**
     * 未登录时添加购物车
     * @param request
     * @param response
     * @param skuId
     * @param userId
     * @param skuNum
     */
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, int skuNum) {

        //查看购物车中是否有商品
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        //如果没有则直接添加到集合！利用有个boolean类型变量处理
        List<CartInfo> cartInfoList=new ArrayList<>();
        boolean ifExist=false;

        if(StringUtils.isNotEmpty(cookieValue)){

            cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);

            for (CartInfo cartInfo : cartInfoList) {

                if(cartInfo.getSkuId().equals(skuId)){

                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);

                    cartInfo.setSkuPrice(cartInfo.getCartPrice());

                    //将变量改为true
                    ifExist=true;
                }
            }
        }

        //购物车里没有对应的商品 或者 没有购物车
        if(!ifExist){

            SkuInfo skuInfo = managerService.getSkuInfo(skuId);
            CartInfo cartInfo=new CartInfo();

            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            cartInfoList.add(cartInfo);

        }

        //将最终的集合放入cookie中
        String newCartJson = JSON.toJSONString(cartInfoList);
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);


    }


    public List<CartInfo> getCartList(HttpServletRequest request) {

        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        if(StringUtils.isNotEmpty(cookieValue)){

            List<CartInfo> cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);

            return cartInfoList;
        }

        return null;
    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {

        CookieUtil.deleteCookie(request,response,cookieCartName);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {

        //  取出购物车中的商品
        List<CartInfo> cartList = getCartList(request);

        if(cartList!=null && cartList.size()>0){

            for (CartInfo cartInfo : cartList){

                if (cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setIsChecked(isChecked);
                }

            }
        }

        // 保存到cookie
        String newCartJson = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);

    }
}
