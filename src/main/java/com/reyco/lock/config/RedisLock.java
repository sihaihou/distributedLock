package com.reyco.lock.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.reyco.lock.utils.SnowFlake;

/**
 * @author reyco
 * @date 2021年3月18日---下午5:14:56
 * 
 *       <pre>
 *       redis锁实现
 * 
 *       <pre>
 */
@Component
public class RedisLock implements DistributedLock {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public final static String SET_LUA_SCRIPT = "if redis.call('setnx', KEYS[1],ARGV[1]) == 1 then return redis.call('EXPIRE',KEYS[1],ARGV[2]) else return 0 end";// lua脚本，用来获取分布式锁
	
	public final static String DEL_LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";// lua脚本，用来释放分布式锁
	
	public final static String RENEWAL_LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('EXPIRE',KEYS[1],ARGV[2]) else return 0 end";// lua脚本，用来续约过期时间
	@Autowired
	@Qualifier("distributedLockThread")
	ExecutorService executorService;
	
	/**
	 * 当前线程锁信息
	 */
	private ThreadLocal<LockInfo> lockInfoThreadLocal = new ThreadLocal<>();
	/**
	 * 锁的key
	 */
	private static final String DISTRIBUTED_LOCK_KEY = "distributedLock:";
	/**
	 * key的名称
	 */
	private static final String DISTRIBUTED_LOCK_DEFAULT = "default";
	/**
	 * 锁的超时时间,默认3000
	 */
	private static final Integer DISTRIBUTED_LOCK_EXPIRE = 3000;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Override
	public void lock() {
		LockInfo lockInfo = new LockInfo(DISTRIBUTED_LOCK_KEY+DISTRIBUTED_LOCK_DEFAULT, SnowFlake.getNextId().toString(), DISTRIBUTED_LOCK_EXPIRE);
		lockInfoThreadLocal.set(lockInfo);
		List<String> keys = new ArrayList<>();
		keys.add(lockInfo.getLockKey());
		String valueExpireTime = (lockInfo.getExpireTime()/1000)+"";
		DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>(SET_LUA_SCRIPT,Long.class);
		while(redisTemplate.execute(defaultRedisScript,keys,lockInfo.getLockValue(),valueExpireTime).intValue()==0) {
			try {
				TimeUnit.MILLISECONDS.sleep(lockInfo.getExpireTime()/10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logger.debug("加锁成功,【key:" +lockInfo.getLockKey() + "】,【value:" + lockInfo.getLockValue() + "】,【expireTimeSecond："+lockInfo.getExpireTime()+"】");
		// 招一个看门狗
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				while(lockInfo.getLockValue().equals(redisTemplate.opsForValue().get(lockInfo.getLockKey()))) {
					List<String> keys = new ArrayList<>();
					keys.add(lockInfo.getLockKey());
					DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>(RENEWAL_LUA_SCRIPT,Long.class);
					Long result = redisTemplate.execute(defaultRedisScript,keys,lockInfo.getLockValue(),valueExpireTime);
					if(result.intValue()==1) {
						logger.debug("续约成功,【key:" +lockInfo.getLockKey() + "】,【value:" + lockInfo.getLockValue() + "】,【expireTimeSecond："+lockInfo.getExpireTime()+"】");
					}
					try {
						Thread.sleep(lockInfo.getExpireTime()/3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	@Override
	public void unlock() {
		LockInfo lockInfo = lockInfoThreadLocal.get();
		lockInfoThreadLocal.remove();
		List<String> keys = new ArrayList<>();
		keys.add(lockInfo.getLockKey());
		DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>(DEL_LUA_SCRIPT,Long.class);
		Long integer = redisTemplate.execute(defaultRedisScript,keys,lockInfo.getLockValue());
		if(integer.intValue()==1) {
			logger.debug("解锁成功,【key:" +lockInfo.getLockKey() + "】,【value:" + lockInfo.getLockValue() + "】");
		}
	}
	
	/**
	 * 加锁
	 * 
	 * @param lockKey
	 *            锁的lockKey
	 * @param lockValue
	 *            锁的value值
	 * @param expireTime
	 *            过期时间
	 */
	@Override
	public void lock(String lockKey, String lockValue, int expireTime) {
		List<String> keys = new ArrayList<>();
		keys.add(lockKey);
		String valueExpireTime = (expireTime/1000)+"";
		DefaultRedisScript<Long> setDefaultRedisScript = new DefaultRedisScript<>(SET_LUA_SCRIPT,Long.class);
		while(redisTemplate.execute(setDefaultRedisScript,keys,lockValue,valueExpireTime).intValue()==0) {
			try {
				Thread.sleep(expireTime/10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logger.debug("加锁成功,【key:"+lockKey+"】,【value:"+lockValue+"】,【expireTime:"+expireTime+"】");
		//获取锁成功：招一个看门狗
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(lockValue.equals(redisTemplate.opsForValue().get(lockKey))) {
					List<String> keys = new ArrayList<>();
					keys.add(lockKey);
					DefaultRedisScript<Long> renDefaultRedisScript = new DefaultRedisScript<>(RENEWAL_LUA_SCRIPT,Long.class);
					Long result = redisTemplate.execute(renDefaultRedisScript,keys,lockValue,valueExpireTime);
					if(result.intValue()==1) {
						logger.debug("续约成功,【key:"+lockValue+ "】,【value:"+lockValue+"】,【expireTimeSecond："+valueExpireTime+"】");
					}
					try {
						Thread.sleep(expireTime/3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	/**
	 * 解锁
	 * @param lockKey
	 * @param lockValue
	 */
	@Override
	public void unLock(String lockKey, String lockValue) {
		List<String> keys = new ArrayList<>();
		keys.add(lockKey);
		DefaultRedisScript<Long> delDefaultRedisScript = new DefaultRedisScript<>(DEL_LUA_SCRIPT,Long.class);
		Long integer = redisTemplate.execute(delDefaultRedisScript,keys,lockValue);
		if(integer.intValue()==1) {
			logger.debug("解锁成功,【key:" +lockKey + "】,【value:" + lockValue + "】");
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

	/**
	 * 锁信息
	 * 
	 * @author reyco
	 *
	 */
	static class LockInfo {
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
