package com.reyco.lock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reyco.lock.service.TestService;


@RestController
@RequestMapping("api")
public class TestController {
	
	@Autowired
	private TestService testService;
	
	@GetMapping("mysqlLock")
	public String test() {
		testService.testLock();
		return "test";
	}
	
	@GetMapping("redisLock")
	public String testLock() {
		testService.testLock1();
		return "test";
	}
	
}
