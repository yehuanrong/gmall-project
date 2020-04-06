package com.yhr.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yhr.bean.*;
import com.yhr.service.ListService;
import com.yhr.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManagerService managerService;

    @RequestMapping("/list.html")
    //@ResponseBody
    public String listData(SkuLsParams skuLsParams, HttpServletRequest request){

        //设置每页显示的条数
        skuLsParams.setPageSize(2);

        SkuLsResult skuLsResult = listService.search(skuLsParams);

       // return JSON.toJSONString(skuLsResult);
        //显示商品数据详情
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        //平台属性，属性值查询
        //可以通过聚合的平台属性值Id ，得到平台属性，平台属性值
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> baseAttrInfoList = managerService.getAttrList(attrValueIdList);

        //定义一个面包屑集合
        List<BaseAttrValue> baseAttrValuesList  = new ArrayList<>();

        //判断url后面的参数
        String urlParam=makeUrlParam(skuLsParams);

        // 集合在循环比较时要想删除数据必须使用迭代器itco
        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {

            BaseAttrInfo baseAttrInfo = iterator.next();

            //获取平台属性值集合
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

            for (BaseAttrValue baseAttrValue : attrValueList) {

                //将平台属性值id与skuLsParams中的valueId对比，如果相同，就将数据移除
                if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){

                    for (String valueId : skuLsParams.getValueId()) {

                        //如果相同，就将数据移除
                        if(valueId.equals(baseAttrValue.getId())){

                            iterator.remove();

                            //构建面包屑组成
                            BaseAttrValue baseAttrValueed = new BaseAttrValue();

                            //将平台的属性值名称改为面包屑
                            baseAttrValueed.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());

                            String newUrlParam = makeUrlParam(skuLsParams, valueId);

                            //将新的url放入到baseAttrValueed中
                            baseAttrValueed.setUrlParam(newUrlParam);

                            baseAttrValuesList.add(baseAttrValueed);
                        }

                    }
                }
            }

        }

        //保存分页的数据
        request.setAttribute("pageNo",skuLsParams.getPageNo());
        request.setAttribute("totalPages",skuLsResult.getTotalPages());

        request.setAttribute("baseAttrValuesList ",baseAttrValuesList );

        request.setAttribute("keyword",skuLsParams.getKeyword());

        request.setAttribute("urlParam",urlParam);

        request.setAttribute("skuLsInfoList",skuLsInfoList);

        request.setAttribute("baseAttrInfoList",baseAttrInfoList);

        return "list";

    }

    /**
     * excludeValueIds:点击面包屑时获取的平台属性值id
     * @param skuLsParams
     * @param excludeValueIds
     * @return
     */
    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds) {

        String urlParam="";

        //判断是否根据keyword来查询
        if(skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){

            urlParam+="keyword="+skuLsParams.getKeyword();
        }

        //判断三级分类id
        if(skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){

            if(urlParam.length()>0){

                urlParam+="&";
            }

            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }

        //判断平台属性值id
        if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){

            for (String valueId:skuLsParams.getValueId()) {

                if(excludeValueIds!=null && excludeValueIds.length>0){

                    //获取点击面包屑时的平台属性值id
                    String excludeValueId = excludeValueIds[0];

                    if(excludeValueId.equals(valueId)){

                        continue;
                    }
                }

                if(urlParam.length()>0){

                    urlParam+="&";
                }

                urlParam+="valueId="+valueId;
            }
        }

        return urlParam;
    }
}
