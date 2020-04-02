package com.yhr.gmall.manager.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.SkuInfo;
import com.yhr.bean.SpuImage;
import com.yhr.bean.SpuSaleAttr;
import com.yhr.service.ManagerService;
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
}
