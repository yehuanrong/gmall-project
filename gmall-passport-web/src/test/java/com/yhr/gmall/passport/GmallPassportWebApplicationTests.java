package com.yhr.gmall.passport;

import com.yhr.gmall.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

	@Test
	public void contextLoads() {

		String key="ATGUIGU_GMALL_KEY";

		HashMap<String,Object> map=new HashMap<>();

		map.put("userId","1");
		map.put("nickName","Atguigu");

		String salt="192.168.126.1";

		String encode = JwtUtil.encode(key, map, salt);

		System.out.println(encode);



		//Map<String, Object> decode = JwtUtil.decode(token, key, salt);

		//System.out.println(decode);
	}

}
