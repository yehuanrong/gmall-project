package com.yhr.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SkuLsResult implements Serializable{

    // 用户根据输入的条件查询到的数据，自定义一个输出对象


    List<SkuLsInfo> skuLsInfoList;

    long total;

    long totalPages;

    List<String> attrValueIdList;

}
