package com.reyco.lock.config;

import java.util.concurrent.locks.Lock;

/**
 * @author reyco
 * @date 2021年3月20日---下午4:16:21
 * <pre>
 *	分布式锁
 * <pre>
 */
public interface DistributedLock extends Lock {
	/**
	 * 	加锁
	 * @param lockKey 	   锁的lockKey
	 * @param lockValue  锁的value值
	 * @param expireTime 过期时间
	 */
	void lock(String lockKey, String lockValue, int expireTime);
	/**
	 * 解锁
	 * @param lockKey	   锁的lockKey
	 * @param lockValue  锁的value值
	 */
	void unLock(String lockKey, String lockValue);
}
