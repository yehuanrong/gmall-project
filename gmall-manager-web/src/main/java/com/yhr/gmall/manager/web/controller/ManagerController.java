package com.yhr.gmall.manager.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.*;
import com.yhr.service.ManagerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class ManagerController {

    @Reference
    private ManagerService managerService;

    @RequestMapping("/getCatalog1")
    public List<BaseCatalog1> getCatalog1(){

        return managerService.getCatalog1();
    }

    @RequestMapping("/getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id){

        return managerService.getCatalog2(catalog1Id);
    }

    @RequestMapping("/getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id){

        return managerService.getCatalog3(catalog2Id);
    }

    @RequestMapping("/attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){

        return managerService.getAttrList(catalog3Id);
    }

    //将前台的json数据转换为对象
    @RequestMapping("/saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){

        managerService.saveAttrInfo(baseAttrInfo);
    }

    /*@RequestMapping("/getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId){

        return managerService.getAttrValueList(attrId);
    }*/

    @RequestMapping("/getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){

        BaseAttrInfo baseAttrInfo=managerService.getAttrInfo(attrId);

        return baseAttrInfo.getAttrValueList();
    }

    @RequestMapping("/baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(){

        return managerService.getBaseSaleAttrList();
    }

}
