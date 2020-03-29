package com.yhr.service;

import com.yhr.bean.BaseAttrInfo;
import com.yhr.bean.BaseCatalog1;
import com.yhr.bean.BaseCatalog2;
import com.yhr.bean.BaseCatalog3;

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
}
