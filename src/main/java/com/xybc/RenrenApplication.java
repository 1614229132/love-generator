package com.xybc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xybc.dao")
public class  RenrenApplication {

	public static    void main(String[] args) {
		SpringApplication.run(RenrenApplication.class, args);
	}
}
