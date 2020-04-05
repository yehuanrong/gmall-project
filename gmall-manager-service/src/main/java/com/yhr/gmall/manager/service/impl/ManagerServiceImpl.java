package com.yhr.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yhr.bean.*;
import com.yhr.gmall.config.RedisUtil;
import com.yhr.gmall.manager.constant.ManagerConst;
import com.yhr.gmall.manager.mapper.*;
import com.yhr.service.ManagerService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ManagerServiceImpl implements ManagerService{

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {

        BaseCatalog2 baseCatalog2=new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {

        BaseCatalog3 baseCatalog3=new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

        /*BaseAttrInfo baseAttrInfo=new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        return baseAttrInfoMapper.select(baseAttrInfo);*/

        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        if(baseAttrInfo.getId()!=null && baseAttrInfo.getId().length()>0){

            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else{

            //保存数据 baseAttrInfo
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        //baseAttrValue?先清空，再插入
        BaseAttrValue baseAttrValueDel=new BaseAttrValue();
        baseAttrValueDel.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueDel);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        if(attrValueList !=null && attrValueList.size()>0){

            for (BaseAttrValue baseAttrValue:attrValueList){

                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }

    }


   /* @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {

        BaseAttrValue baseAttrValue=new BaseAttrValue();

        baseAttrValue.setAttrId(attrId);

        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);

        return baseAttrValueList;
    }*/

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {

        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        BaseAttrValue baseAttrValue=new BaseAttrValue();

        baseAttrValue.setAttrId(attrId);

        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);

        baseAttrInfo.setAttrValueList(baseAttrValueList);

        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuList(SpuInfo spuInfo) {

        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);
        return spuInfoList;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {

        spuInfoMapper.insertSelective(spuInfo);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();

        if(spuImageList!=null && spuImageList.size()>0){

            for (SpuImage spuImage : spuImageList) {

                //设置spuId
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();

        if(spuSaleAttrList!=null && spuSaleAttrList.size()>0){

            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {

                spuSaleAttr.setSpuId(spuInfo.getId());

                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();

                if(spuSaleAttrValueList!=null && spuSaleAttrValueList.size()>0){

                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {

                        spuSaleAttrValue.setSpuId(spuInfo.getId());

                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }

    }

    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {

        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {

        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {

        skuInfoMapper.insertSelective(skuInfo);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();

        if(skuImageList!=null && skuImageList.size()>0){

            for (SkuImage skuImage : skuImageList) {

                skuImage.setSkuId(skuInfo.getId());

                skuImageMapper.insertSelective(skuImage);
            }
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();

        if(skuAttrValueList!=null && skuAttrValueList.size()>0){

            for (SkuAttrValue skuAttrValue : skuAttrValueList) {

                skuAttrValue.setSkuId(skuInfo.getId());

                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();

        if(skuSaleAttrValueList!=null && skuSaleAttrValueList.size()>0){

            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {

                skuSaleAttrValue.setSkuId(skuInfo.getId());

                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {

        SkuInfo skuInfo=null;

        Jedis jedis= null;

        RLock mylock=null;

        try {

            /*
            * 使用redisson分布式锁解决缓存击穿问题
            * */

            Config config=new Config();

            config.useSingleServer().setAddress("redis://192.168.126.11:6379");

            RedissonClient redissonClient= Redisson.create(config);

            mylock = redissonClient.getLock("mylock");

            mylock.lock(10, TimeUnit.SECONDS);

            jedis = redisUtil.getJedis();

            String skuKey= ManagerConst.SKUKEY_PREFIX+skuId+ManagerConst.SKUKEY_SUFFIX;

            if (jedis.exists(skuKey)){

                String skuInfoJson = jedis.get(skuKey);

                skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);

                return skuInfo;
            }else{

                skuInfo = getSkuInfoDB(skuId);

                String jsonString = JSON.toJSONString(skuInfo);

                jedis.setex(skuKey,ManagerConst.SKUKEY_TIMEOUT,jsonString);

                return skuInfo;
            }

        }catch (Exception e){

            e.printStackTrace();

        }finally {

            if(jedis!=null){

                jedis.close();
            }

            if(mylock!=null){

                mylock.unlock();
            }
        }
        /*try {
            jedis = redisUtil.getJedis();

        *//** 设计redis，必须注意使用哪种数据类型来存储数据
        * redis的五种数据类型
        *   string:短信验证码，存储一个变量
        *
        *   hash:json字符串（对象转换的字符串）
        *
        *   list:pop队列使用
        *
        *   set:去重，交集，补集，并集
        *
        *   zset:评分，排序
        * *//*
            //(定义key)
            String skuKey= ManagerConst.SKUKEY_PREFIX+skuId+ManagerConst.SKUKEY_SUFFIX;

            //获取数据
            String skuJson=jedis.get(skuKey);

            //判断缓存中是否有数据，如果有，从缓存中获取
            if(skuJson==null || skuJson.length()==0){

                //试着加锁
                String skuLockKey=ManagerConst.SKUKEY_PREFIX+skuId+ManagerConst.SKULOCK_SUFFIX;

                String lockKey = jedis.set(skuLockKey, "ok", "NX", "PX", ManagerConst.SKULOCK_EXPIRE_PX);

                if("ok".equals(lockKey)){

                    skuInfo=getSkuInfoDB(skuId);

                    String skuRedisStr = JSON.toJSONString(skuInfo);

                    jedis.setex(skuKey,ManagerConst.SKUKEY_TIMEOUT,skuRedisStr);

                    //删除lockKey
                    jedis.del(skuLockKey);

                    return skuInfo;

                }else{

                    //等待

                    Thread.sleep(1000);
                    return getSkuInfo(skuId);
                }


            }else{

                //将json字符串转换成对象
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);

                return skuInfo;

            }
        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if(jedis!=null){

                jedis.close();
            }

        }*/

        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(String skuId){

        SkuInfo skuInfo=skuInfoMapper.selectByPrimaryKey(skuId);

        skuInfo.setSkuImageList(getSkuImageBySkuId(skuId));

        //查询平台属性值集合
        SkuAttrValue skuAttrValue=new SkuAttrValue();

        skuAttrValue.setSkuId(skuId);

        skuInfo.setSkuAttrValueList(skuAttrValueMapper.select(skuAttrValue));

        return skuInfo;
    }

    @Override
    public List<SkuImage> getSkuImageBySkuId(String skuId) {

        SkuImage skuImage=new SkuImage();

        skuImage.setSkuId(skuId);

        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);

        return skuImageList;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {

        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {

        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

}
