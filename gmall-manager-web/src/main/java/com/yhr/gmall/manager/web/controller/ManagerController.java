package com.yhr.gmall.manager.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.BaseAttrInfo;
import com.yhr.bean.BaseCatalog1;
import com.yhr.bean.BaseCatalog2;
import com.yhr.bean.BaseCatalog3;
import com.yhr.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class ManagerController {

    @Reference
    private ManagerService managerService;

    @RequestMapping("/getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){

        return managerService.getCatalog1();
    }

    @RequestMapping("/getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id){

        return managerService.getCatalog2(catalog1Id);
    }

    @RequestMapping("/getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id){

        return managerService.getCatalog3(catalog2Id);
    }

    @RequestMapping("/attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){

        return managerService.getAttrList(catalog3Id);
    }
}
