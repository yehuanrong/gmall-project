package com.yhr.gmall.manager.mapper;

import com.yhr.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue>{

    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);
}
