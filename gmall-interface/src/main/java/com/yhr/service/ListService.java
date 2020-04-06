package com.yhr.service;

import com.yhr.bean.SkuLsInfo;
import com.yhr.bean.SkuLsParams;
import com.yhr.bean.SkuLsResult;

public interface ListService {

    /**
     * 保存数据到es中
     * @param skuLsInfo
     */
    void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    /**
     * 检索数据
     * @param skuLsParams
     * @return
     */
    SkuLsResult search(SkuLsParams skuLsParams);

    void incrHotScore(String skuId);
}
