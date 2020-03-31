package com.yhr.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.yhr.bean.*;
import com.yhr.gmall.manager.mapper.*;
import com.yhr.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        BaseAttrInfo baseAttrInfo=new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        return baseAttrInfoMapper.select(baseAttrInfo);
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

}
