package com.reyco.lock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.reyco.lock.dao")
@SpringBootApplication
public class LokcApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(LokcApplication.class, args);
	}
	
}
