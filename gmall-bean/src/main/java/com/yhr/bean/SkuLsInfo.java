package com.yhr.bean;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 这个bean与es中自定义的mapping相对应
 */
@Data
public class SkuLsInfo implements Serializable{

    //不加注解因为不是数据库的表
    String id;

    BigDecimal price;

    String skuName;

    String catalog3Id;

    String skuDefaultImg;

    //自定义一个字段来保存热度评分
    Long hotScore=0L;

    List<SkuLsAttrValue> skuAttrValueList;


}
