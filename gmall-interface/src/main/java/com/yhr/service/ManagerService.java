package com.yhr.service;

import com.yhr.bean.*;

import java.util.List;

public interface ManagerService {

    /**
     * 获取所有的一级分类数据
     * @return
     */
    List<BaseCatalog1> getCatalog1();

    /**
     * 根据一级分类id获取二级分类数据
     * @param catalog1Id
     * @return
     */
    List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 根据二级分类id获取三级分类数据
     * @param catalog2Id
     * @return
     */
    List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 根据三级分类的id查询平台属性集合
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 保存平台属性数据
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据平台属性id查询平台属性值
     * @param attrId
     * @return
     */
    //List<BaseAttrValue> getAttrValueList(String attrId);

    /**
     * 根据平台属性id查询平台属性对象
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(String attrId);

    /**
     * 根据spuinfo对象获取spuinfo集合
     * @param spuInfo
     * @return
     */
    List<SpuInfo> getSpuList(SpuInfo spuInfo);

    /**
     * 获取所有的销售数据
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存spuInfo
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);
}
