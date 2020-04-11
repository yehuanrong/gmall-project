package com.yhr.gmall.cart.service.mapper;

import com.yhr.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartMapper extends Mapper<CartInfo>{

    /**
     * 根据用户id查询实时价格到cartInfo
     * @param userId
     * @return
     */
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
