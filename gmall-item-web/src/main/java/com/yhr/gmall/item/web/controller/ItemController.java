package com.yhr.gmall.item.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.yhr.bean.SkuImage;
import com.yhr.bean.SkuInfo;
import com.yhr.bean.SkuSaleAttrValue;
import com.yhr.bean.SpuSaleAttr;
import com.yhr.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    private ManagerService managerService;

    @RequestMapping("/{skuId}.html")
    public String getSkuInfo(@PathVariable String skuId, HttpServletRequest request){

        SkuInfo skuInfo=managerService.getSkuInfo(skuId);

        //查询图片列表
        List<SkuImage> skuImageList=managerService.getSkuImageBySkuId(skuId);

        //查询销售属性，销售属性值集合
        List<SpuSaleAttr> spuSaleAttrList=managerService.getSpuSaleAttrListCheckBySku(skuInfo);

        //获取销售属性值id
        List<SkuSaleAttrValue> skuSaleAttrValueList=managerService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        String key="";
        HashMap<String,Object> map=new HashMap<>();
        for(int i=0;i<skuSaleAttrValueList.size();i++){

            SkuSaleAttrValue skuSaleAttrValue=skuSaleAttrValueList.get(i);

            if(key.length()>0){

                key+="|";
            }
            key+=skuSaleAttrValue.getSaleAttrValueId();

            if((i+1)==skuSaleAttrValueList.size() || !skuSaleAttrValue.getSkuId().equals( skuSaleAttrValueList.get(i+1).getSkuId())){

                //放入map集合
                map.put(key,skuSaleAttrValue.getSkuId());

                //清空key
                key="";
            }

        }

        //将map转化成json字符串
        String valuesSkuJson= JSON.toJSONString(map);

        request.setAttribute("valuesSkuJson",valuesSkuJson);


        request.setAttribute("spuSaleAttrList",spuSaleAttrList);

        request.setAttribute("skuImageList",skuImageList);

        request.setAttribute("skuInfo",skuInfo);

        return "item";

    }

}
