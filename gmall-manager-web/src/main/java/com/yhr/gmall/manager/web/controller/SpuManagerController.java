package com.yhr.gmall.manager.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.SpuInfo;
import com.yhr.service.ManagerService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SpuManagerController {

    @Reference
    private ManagerService managerService;

    @RequestMapping("/spuList")
    public List<SpuInfo> spuList(SpuInfo spuInfo){

        return managerService.getSpuList(spuInfo);
    }

    @RequestMapping("/saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){

        if(spuInfo!=null){

            managerService.saveSpuInfo(spuInfo);
        }

    }
}
