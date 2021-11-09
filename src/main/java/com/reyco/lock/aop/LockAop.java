package com.reyco.lock.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.reyco.lock.annotation.Lock;
import com.reyco.lock.config.DistributedLock;
import com.reyco.lock.utils.SnowFlake;

@Aspect
@Component
public class LockAop {
	
	@Autowired
	private DistributedLock distributedLock;
	
	@Autowired
	private RedissonClient redissonClient;

	@Pointcut("@annotation(com.reyco.lock.annotation.Lock)")
	public void lockPointCut() {
	}

	@Around("lockPointCut()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method targetMethod = signature.getMethod();
		Lock lock = targetMethod.getAnnotation(Lock.class);
		if(lock!=null) {
			String lockValue = SnowFlake.getNextId().toString();
			//distributedLock.lock(lock.value(), lockValue, lock.expireTime());
			distributedLock.lock();
			Object proceed = joinPoint.proceed();
			//distributedLock.unLock(lock.value(), lockValue);
			distributedLock.unlock();
			return proceed;
		}
		return joinPoint.proceed();
	}
	/*@Around("lockPointCut()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method targetMethod = signature.getMethod();
		Lock lock = targetMethod.getAnnotation(Lock.class);
		if(lock!=null) {
			RLock lock2 = redissonClient.getLock(lock.value());
			lock2.lock();
			Object proceed = null;
			try {
				proceed = joinPoint.proceed();
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				lock2.unlock();
			}
			return proceed;
		}
		return joinPoint.proceed();
	}*/
}
