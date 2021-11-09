package com.reyco.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAspectJAutoProxy
@EnableAsync
@MapperScan("com.reyco.lock.dao")
@SpringBootApplication
public class LokcApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(LokcApplication.class, args);
	}
	/**
     * 自定义异步线程池
     * @return
     */
    @Bean
    public AsyncTaskExecutor taskExecutor() {  
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(); 
        executor.setThreadNamePrefix("Async-Executor");
        executor.setMaxPoolSize(10);  
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // .....
            }
        });
        // 使用预定义的异常处理类
        // executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;  
    }
    /**
     * 分布式锁续约的线程池对象
     * @return
     */
    @Bean("distributedLockThread")
    public ExecutorService distributedLockThread() {
    	ExecutorService distributedLockThread = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,new LinkedBlockingQueue<>(100000),new ThreadFactory() {
    		@Override
    		public Thread newThread(Runnable r) {
    			Thread t = new Thread(r);
    			t.setDaemon(true);
    			t.setName("distributedLock-thread");
    			return t;
    		}
    	},new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // .....
            }
        });
    	return distributedLockThread;
    }
}
