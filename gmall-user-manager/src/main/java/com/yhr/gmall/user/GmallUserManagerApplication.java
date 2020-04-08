package com.yhr.gmall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = "com.yhr.gmall.user.mapper")
@SpringBootApplication
@ComponentScan(basePackages = "com.yhr.gmall")
public class GmallUserManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallUserManagerApplication.class, args);
	}

}
