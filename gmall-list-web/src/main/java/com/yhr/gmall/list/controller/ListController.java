package com.yhr.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.yhr.bean.SkuLsParams;
import com.yhr.bean.SkuLsResult;
import com.yhr.service.ListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @RequestMapping("/list.html")
    @ResponseBody
    public String listData(SkuLsParams skuLsParams){

        SkuLsResult skuLsResult = listService.search(skuLsParams);

        return JSON.toJSONString(skuLsResult);
    }
}
