package com.yhr.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yhr.bean.CartInfo;
import com.yhr.bean.SkuInfo;
import com.yhr.gmall.cart.service.constant.CartConst;
import com.yhr.gmall.cart.service.mapper.CartMapper;
import com.yhr.gmall.config.RedisUtil;
import com.yhr.service.CartService;
import com.yhr.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService{
    
    @Autowired
    private CartMapper cartMapper;
    
    @Reference
    private ManagerService managerService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 在控制器中判断用户是否登录
     * 登录时添加购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        /*
        * 先查询购物车中是否有相同的商品，如果有就数量相加
        * 如果没有直接添加到数据库
        * 更新缓存
        * */
        //通过skuId和userId查询是否有该商品

        Jedis jedis=redisUtil.getJedis();
        String cartKey= CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        CartInfo cartInfo=new CartInfo();
        
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);

        CartInfo cartInfoExist  = cartMapper.selectOne(cartInfo);

        if(cartInfoExist!=null){

            //数量相加
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);

            //给skuPrice初始化
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());

            //更新数据
            cartMapper.updateByPrimaryKeySelective(cartInfoExist);

            //同步缓存
        }else {

            //没有就直接添加购物车
            //如果不存在，保存购物车
            SkuInfo skuInfo = managerService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);

            cartMapper.insertSelective(cartInfo1);

            cartInfoExist=cartInfo1;

        }

        //将数据放入缓存
        jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfoExist));

        //设置购物车的缓存过期时间 与用户的过期时间一致
        String userKey=CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;

        //获取用户的过期时间
        Long userTtl = jedis.ttl(userKey);

        //给购物车设置过期时间
        jedis.expire(cartKey,userTtl.intValue());

        jedis.close();

    }

    @Override
    public List<CartInfo> getCartList(String userId) {

        Jedis jedis = redisUtil.getJedis();

        String cartKey= CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        List<String> cartJsons = jedis.hvals(cartKey);//返回list集合

        if (cartJsons!=null && cartJsons.size()>0){

            List<CartInfo> cartInfoList = new ArrayList<>();

            for (String cartJson : cartJsons) {

                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);

                cartInfoList.add(cartInfo);
            }

            //查看时排序（真实项目利用更新时间排序），用id模拟

            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {

                    return o1.getId().compareTo(o2.getId());
                }
            });

            return cartInfoList;
        }else {

            //从数据库中获取数据，并添加到缓存
            List<CartInfo> cartInfoList = loadCartCache(userId);
            return  cartInfoList;


        }

    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListCk, String userId) {

        //根据userId获取购物车数据
        List<CartInfo> cartInfoListDB = cartMapper.selectCartListWithCurPrice(userId);

        //合并的条件是skuId相同
        // 循环开始匹配
        for (CartInfo cartInfoCk : cartListCk) {

            boolean isMatch =false;

            for (CartInfo cartInfoDB : cartInfoListDB){

                if(cartInfoDB.getSkuId().equals(cartInfoCk.getSkuId())){

                    cartInfoDB.setSkuNum(cartInfoCk.getSkuNum()+cartInfoDB.getSkuNum());

                    cartMapper.updateByPrimaryKeySelective(cartInfoDB);

                    isMatch=true;
                }
            }

            //没有匹配上，未登录的用户
            if(isMatch){

                cartInfoCk.setUserId(userId);
                cartMapper.insertSelective(cartInfoCk);

            }
        }

        List<CartInfo> cartInfoList = loadCartCache(userId);

        for (CartInfo cartInfoDB : cartInfoList) {
            for (CartInfo info : cartListCk) {
                if (cartInfoDB.getSkuId().equals(info.getSkuId())){
                    // 只有被勾选的才会进行更改
                    if (info.getIsChecked().equals("1")){
                        cartInfoDB.setIsChecked(info.getIsChecked());
                        // 更新redis中的isChecked
                        checkCart(cartInfoDB.getSkuId(),"1",userId);
                    }
                }
            }
        }

        return cartInfoList;

    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {

        /*
        * 1.获取jedis
        * 2.获取购物车集合
        * 3.修改商品的勾选状态（isChecked）
        * 4.写回购物车
        * 5.新建一个购物车来存储勾选的商品
        * */

        Jedis jedis = redisUtil.getJedis();

        String cartKey= CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        String cartInfoJson = jedis.hget(cartKey, skuId);

        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);

        cartInfo.setIsChecked(isChecked);

        jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfo));

        //新建一个购物车key来存储勾选的商品
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;

        if("1".equals(isChecked)){

            jedis.hset(userCheckedKey,skuId,JSON.toJSONString(cartInfo));
        }else {

            //删除被勾选的商品
            jedis.hdel(userCheckedKey,skuId);
        }

        jedis.close();
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {

        /*
        * 获取被选中的购物车集合
        *   1.获取jedis
        *   2.定义key
        *   3.获取数据返回
        * */
        Jedis jedis = redisUtil.getJedis();

        List<CartInfo> cartInfoList=new ArrayList<>();

        String cartKey= CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        List<String> stringList = jedis.hvals(cartKey);

        if(stringList!=null && stringList.size()>0){

            for (String cartJson : stringList) {

                cartInfoList.add(JSON.parseObject(cartJson,CartInfo.class));
            }
        }

        jedis.close();

        return cartInfoList;
    }

    /**
     * 根据userId查询购物车
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {

        List<CartInfo> cartInfoList = cartMapper.selectCartListWithCurPrice(userId);

        if(cartInfoList==null && cartInfoList.size()>0){

            return null;
        }

        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();

        Map<String,String> map = new HashMap<>();

        for (CartInfo cartInfo : cartInfoList) {
            String cartJson = JSON.toJSONString(cartInfo);
            // key 都是同一个，值会产生重复覆盖！
            map.put(cartInfo.getSkuId(),cartJson);
        }

        // 将java list - redis hash
        jedis.hmset(userCartKey,map);
        jedis.close();

        return cartInfoList;

    }
}
