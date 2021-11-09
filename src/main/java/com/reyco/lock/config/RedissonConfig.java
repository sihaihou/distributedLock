package com.reyco.lock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author reyco
 * @date 2021.09.10
 * @version v1.0.1
 */
@Configuration
public class RedissonConfig {

	@Bean
	public RedissonClient redisClient() {
		Config config = new Config();
		config.useSingleServer()
		.setAddress("redis://47.114.74.174:7001")
		.setDatabase(0)
		.setPassword("Reyco123456.")
		.setConnectionPoolSize(200)
		.setTimeout(10000)
		.setConnectTimeout(10000)
		.setConnectionMinimumIdleSize(20)
		.setIdleConnectionTimeout(200000);
		RedissonClient redissonClient = Redisson.create(config);
		return redissonClient;
	}
}
