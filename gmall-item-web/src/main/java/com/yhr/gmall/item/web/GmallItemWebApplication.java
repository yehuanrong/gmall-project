package com.yhr.gmall.item.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.yhr.gmall")
public class GmallItemWebApplication {

	public static void main(String[] args) {

		SpringApplication.run(GmallItemWebApplication.class, args);
	}

}
