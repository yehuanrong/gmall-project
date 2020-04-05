package com.yhr.gmall.list.service;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	@Autowired
	private JestClient jestClient;

	@Test
	public void contextLoads() throws IOException {

		/**
		 * 测试能否与es连通
		 * 	1.定义dsl语句
		 * 	2.定义执行的动作
		 * 	3.执行动作
		 * 	4.获取执行之后的结果集
		 */

		String query="{\n" +
				"  \"query\": {\n" +
				"    \"match\": {\n" +
				"      \"name\": \"红海战役\"\n" +
				"    }\n" +
				"  }\n" +
				"}";

		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();

		SearchResult searchResult = jestClient.execute(search);

		List<SearchResult.Hit<Map, Void>> hits = searchResult.getHits(Map.class);

		for (SearchResult.Hit<Map, Void> hit : hits) {

			Map map = hit.source;

			System.out.println(map.get("name"));
		}

	}

}
