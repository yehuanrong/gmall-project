package com.yhr.service;

import com.yhr.bean.CartInfo;

import java.util.List;

public interface CartService {

    void  addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 根据用户id查询购物车数据
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 合并购物车
     * @param cartListCk
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListCk, String userId);

    /**
     * 修改商品状态
     * @param skuId
     * @param isChecked
     * @param userId
     */
    void checkCart(String skuId, String isChecked, String userId);

    /**
     * 根据用户id查询勾选的购物车
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);

    /**
     * 通过userId查询实时价格
     * @param userId
     * @return
     */
    List<CartInfo> loadCartCache(String userId);

}
