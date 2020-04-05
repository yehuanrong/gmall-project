package com.yhr.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuLsParams implements Serializable{

    // 自定义用户输入参数实体类
    //keyword相当于skuName
    String  keyword;

    String catalog3Id;

    String[] valueId;

    int pageNo=1;

    int pageSize=20;

}
