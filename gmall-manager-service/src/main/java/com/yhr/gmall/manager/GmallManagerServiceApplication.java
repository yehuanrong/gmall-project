package com.yhr.gmall.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = "com.yhr.gmall.manager.mapper")
@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = "com.yhr.gmall")
public class GmallManagerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallManagerServiceApplication.class, args);
	}

}
