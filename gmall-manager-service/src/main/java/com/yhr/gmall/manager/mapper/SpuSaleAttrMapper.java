package com.yhr.gmall.manager.mapper;

import com.yhr.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr>{

    List<SpuSaleAttr> selectSpuSaleAttrList(String spuId);
}
