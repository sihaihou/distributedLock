package com.reyco.lock.config;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.reyco.lock.utils.SnowFlake;

/**
*@author reyco
*@date  2021年3月18日---下午5:14:56
*<pre>
*	redis锁实现
*<pre> 
*/
@Component
public class RedisLock implements DistributedLock,InitializingBean{
	/**
	 * 当前线程锁信息
	 */
	private ThreadLocal<LockInfo> lockInfoThreadLocal = new ThreadLocal<>();
	/**
	 * 锁的key
	 */
	private static final String DISTRIBUTED_LOCK_KEY = "distributed:lock:key";
	/**
	 * 锁的超时时间
	 */
	private static final Integer DISTRIBUTED_LOCK_EXPIRE = 10;
	
	@Autowired
	private StringRedisTemplate redisTemplate;
	
	private ValueOperations<String, String> opsForValue;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		opsForValue = redisTemplate.opsForValue();
	}
	
	@Override
	public void lock() {
		String lockKey = DISTRIBUTED_LOCK_KEY;
		String lockValue = SnowFlake.getNextId().toString();
		Integer expireTime = DISTRIBUTED_LOCK_EXPIRE;
		LockInfo lockInfo = new LockInfo(lockKey,lockValue,expireTime);
		lockInfoThreadLocal.set(lockInfo);
		if(!opsForValue.setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.SECONDS)) {
			lock(lockKey, lockValue, expireTime);
		}
		//招一个看门狗
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(lockValue.equals(opsForValue.get(lockKey))) {
		            int sleepTime = expireTime / 3;
		            try {
		                Thread.sleep(sleepTime * 1000);
		            } catch (InterruptedException e) {
		                e.printStackTrace();
		            }
		            redisTemplate.expire(lockKey, expireTime,  TimeUnit.SECONDS);
				}
			}
		}).start();
		
	}
	/**
	 * 加锁
	 * @param lockKey		锁的lockKey	
	 * @param lockValue		锁的value值
	 * @param expireTime	过期时间
	 */
	public void lock(String lockKey,String lockValue,int expireTime) {
		if(!opsForValue.setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.SECONDS)) {
			lock(lockKey, lockValue, expireTime);
		}
		//招一个看门狗
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(lockValue.equals(opsForValue.get(lockKey))) {
		            int sleepTime = expireTime / 3;
		            try {
		                Thread.sleep(sleepTime * 1000);
		            } catch (InterruptedException e) {
		                e.printStackTrace();
		            }
		            redisTemplate.expire(lockKey, expireTime,  TimeUnit.SECONDS);
				}
			}
		}).start();
	}
	@Override
	public void unlock() {
		LockInfo lockInfo = lockInfoThreadLocal.get();
		if(lockInfo.getLockValue().equals(opsForValue.get(lockInfo.getLockKey()))) {
			redisTemplate.delete(lockInfo.getLockKey());
		}
		lockInfoThreadLocal.remove();
	}
	/**
	 * 解锁
	 * @param lockKey
	 * @param lockValue
	 */
	public void unLock(String lockKey,String lockValue) {
		if(lockValue.equals(opsForValue.get(lockKey))) {
			redisTemplate.delete(lockKey);
		}
	}
	
	@Override
	public void lockInterruptibly() throws InterruptedException {
	}
	
	@Override
	public boolean tryLock() {
		return false;
	}
	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return false;
	}
	@Override
	public Condition newCondition() {
		return null;
	}
	static class LockInfo{
		private String lockKey;
		private String lockValue;
		private Integer expireTime;
		
		public LockInfo(String lockKey, String lockValue, Integer expireTime) {
			super();
			this.lockKey = lockKey;
			this.lockValue = lockValue;
			this.expireTime = expireTime;
		}
		public String getLockKey() {
			return lockKey;
		}
		public void setLockKey(String lockKey) {
			this.lockKey = lockKey;
		}
		public String getLockValue() {
			return lockValue;
		}
		public void setLockValue(String lockValue) {
			this.lockValue = lockValue;
		}
		public Integer getExpireTime() {
			return expireTime;
		}
		public void setExpireTime(Integer expireTime) {
			this.expireTime = expireTime;
		}
	}
}
