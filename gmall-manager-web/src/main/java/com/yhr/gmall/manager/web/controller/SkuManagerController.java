package com.yhr.gmall.manager.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.SkuInfo;
import com.yhr.bean.SkuLsInfo;
import com.yhr.bean.SpuImage;
import com.yhr.bean.SpuSaleAttr;
import com.yhr.service.ListService;
import com.yhr.service.ManagerService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SkuManagerController {

    @Reference
    private ManagerService managerService;

    @Reference
    private ListService listService;

    @RequestMapping("/spuImageList")
    public List<SpuImage> spuImageList(SpuImage spuImage){

        return managerService.getSpuImageList(spuImage);
    }

    @RequestMapping("/spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){

        return managerService.getSpuSaleAttrList(spuId);
    }

    @RequestMapping("/saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo){

        if(skuInfo!=null){

            managerService.saveSkuInfo(skuInfo);
        }

    }

    @RequestMapping("/onSale")
    public void onSale(String skuId){

        SkuLsInfo skuLsInfo=new SkuLsInfo();

        SkuInfo skuInfo= managerService.getSkuInfo(skuId);

        //属性拷贝
        BeanUtils.copyProperties(skuInfo,skuLsInfo);

        listService.saveSkuLsInfo(skuLsInfo);
    }
}
