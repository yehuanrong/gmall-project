package com.yhr.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.yhr.bean.SkuLsInfo;
import com.yhr.bean.SkuLsParams;
import com.yhr.bean.SkuLsResult;
import com.yhr.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService{

    @Autowired
    private JestClient jestClient;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";


    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {

        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {

        String query=makeQueryStringForSearch(skuLsParams);

        Search build = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();

        SearchResult searchResult=null;

        try {
            searchResult = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams, searchResult);

        return skuLsResult;
    }

    //设置返回结果
    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {

        SkuLsResult skuLsResult=new SkuLsResult();

        //声明一个集合来存储SkuLsInfo
        ArrayList<SkuLsInfo> skuLsInfoArrayList=new ArrayList<>();
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        //循环遍历
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {

            SkuLsInfo skuLsInfo = hit.source;

            //获取skuName的高亮
            if(hit.highlight!=null && hit.highlight.size()>0){

                Map<String, List<String>> highlight = hit.highlight;
                List<String> stringList = highlight.get("skuName");

                //高亮的skuName
                String skuNameHl = stringList.get(0);

                skuLsInfo.setSkuName(skuNameHl);
            }
            skuLsInfoArrayList.add(skuLsInfo);
        }

        skuLsResult.setSkuLsInfoList(skuLsInfoArrayList);

        skuLsResult.setTotal(searchResult.getTotal());

        //计算总页数
        long totalPage= (searchResult.getTotal() + skuLsParams.getPageSize() -1) / skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPage);

        //获取平台属性值id
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();

        //声明一个集合来存储valueId
        ArrayList<String> stringArrayListId=new ArrayList<>();
        for (TermsAggregation.Entry bucket : buckets) {

            String valueId=bucket.getKey();

            stringArrayListId.add(valueId);
        }

        skuLsResult.setAttrValueIdList(stringArrayListId);

        return skuLsResult;

    }


    //动态生成dsl语句
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {

        //定义一个查询器
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();

        //创建一个bool
        BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery();

        //判断三级分类id
        if(skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            //创建term
            TermQueryBuilder termQueryBuilder=new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id());

            //创建filter
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //判断平台属性值id
        if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){

            for (String valueId :skuLsParams.getValueId() ) {
                //创建term
                TermQueryBuilder termQueryBuilder=new TermQueryBuilder("skuAttrValueList.valueId",valueId);

                //创建filter
                boolQueryBuilder.filter(termQueryBuilder);
            }

        }

        //判断keyword是否为空
        if(skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){

            //创建match
            MatchQueryBuilder matchQueryBuilder=new MatchQueryBuilder("skuName",skuLsParams.getKeyword());

            //创建must
            boolQueryBuilder.must(matchQueryBuilder);

            //设置高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();

            //设置高亮的规则
            highlighter.field("skuName");
            highlighter.preTags("<span style=color:red>");
            highlighter.postTags("</span>");

            //将设置好的高亮对象放到查询器中
            searchSourceBuilder.highlight(highlighter);

        }

        //创建query
        searchSourceBuilder.query(boolQueryBuilder);

        //创建分页
        //从第几条开始查询
        searchSourceBuilder.from((skuLsParams.getPageNo()-1)*skuLsParams.getPageSize());

        //每页查询多少条
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //设置排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //设置聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr");

        groupby_attr.field("skuAttrValueList.valueId");

        searchSourceBuilder.aggregation(groupby_attr);

        String query=searchSourceBuilder.toString();

        return query;

    }
}
