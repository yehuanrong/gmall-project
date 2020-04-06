package com.yhr.gmall.manager.mapper;

import com.yhr.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo>{

    //根据三级分类id查询平台属性集合
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    //平台属性值id查询
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("valueIds") String valueIds);
}
