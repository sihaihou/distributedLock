package com.reyco.lock.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
*@author reyco
*@date  2021年3月20日---上午11:29:35
*<pre>
*
*<pre> 
*/
public class SnowFlake {
	/**
	 * 
	 */
	private static volatile SnowFlake instance;
	
	/**
	 * 起始的时间戳
	 */
	private final static long START_STMP = 1595688681000L;
	/**
	 * 每一部分占用的位数
	 */
	private final static long MACHINE_BIT = 5; 		//机器标识占用的位数
	private final static long workId_BIT = 5; 		//工作id占用的位数
	private final static long SEQUENCE_BIT = 12; 	//序列号占用的位数
	/**
	 * 每一部分的最大值
	 */
	private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT); 		//机器标识最大值
	private final static long MAX_workId_NUM = -1L ^ (-1L << workId_BIT);   		//工作id最大值
	private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);			//序列号最大值
	/**
	 * 每一部分向左的位移
	 */
	private final static long WORKId_LEFT = SEQUENCE_BIT;						//工作id向左的位移：12=12
	private final static long MACHINE_LEFT = SEQUENCE_BIT + workId_BIT;			//机器标识向左的位移：17=12+5
	private final static long TIMESTMP_LEFT = MACHINE_LEFT + MACHINE_BIT;     	//时间戳向左的位移：     22=17+5
	/**
	 * 工作id
	 */
	private long workId;
	/**
	 * 机器标识
	 */
	private long machineId;
	/**
	 * 序列号
	 */
	private long sequence = 0L;
	/**
	 * 上一次时间戳
	 */
	private long lastStmp = -1L;

	private SnowFlake(long machineId,long workId) {
		if (workId > MAX_workId_NUM || workId < 0) {
			throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
		}
		if (machineId > MAX_MACHINE_NUM || machineId < 0) {
			throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
		}
		this.workId = workId;
		this.machineId = machineId;
	}
	/**
	 * 产生下一个ID
	 *
	 * @return
	 */
	public synchronized long nextId() {
		long currStmp = getNewstmp();
		if (currStmp < lastStmp) {
			// 时钟倒流抛出异常
			throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
		}
		if (currStmp == lastStmp) {
			// 相同毫秒内，序列号自增
			sequence = (sequence + 1) & MAX_SEQUENCE;
			// 同一毫秒的序列数已经达到最大
			if (sequence == 0) {
				currStmp = getNextMill(lastStmp);
			}
		} else {
			// 不同毫秒内，序列号置为0
			sequence = 0L;
		}

		lastStmp = currStmp;

		return (currStmp - START_STMP) << TIMESTMP_LEFT // 时间戳部分
				| machineId << MACHINE_LEFT // 机器标识部分
				| workId << WORKId_LEFT // 工作id部分
				| sequence; // 序列号部分
	}
	
	/**
	 * 阻塞到下一个毫秒，直到获得新的时间戳
	 * @return	当前时间戳
	 */
	private long getNextMill(long lastStmp) {
		long timestamp  = getNewstmp();
		while (timestamp  <= lastStmp) {
			timestamp  = getNewstmp();
		}
		return timestamp ;
	}
	/**
	 *  返回以毫秒为单位的当前时间
	 * @return 当前时间(毫秒)
	 */
	private long getNewstmp() {
		return System.currentTimeMillis();
	}
	
	
	/**
	 * 单例获取该对象
	 * @param machineId
	 * @param workId
	 * @return
	 */
	private static SnowFlake getSnowFlake(long machineId,long workId) {
		if(instance==null) {
			synchronized (SnowFlake.class) {
				if(instance==null) {
					instance = new SnowFlake(machineId, workId);
				}
			}
		}
		return instance;
	}
	/**
	 * 获取获取id
	 * @return
	 */
	public static Long getNextId() {
		SnowFlake snowFlake = getSnowFlake(0,0);
		long nextId = snowFlake.nextId();
		//System.out.println(nextId);
		return nextId;
	}
	/**
	 * 测试
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		test1();
	}
	private static void test2() throws InterruptedException {
		Map<Long,Integer> map =new ConcurrentHashMap<>();
		
		CountDownLatch cdl = new CountDownLatch(5000);
		List<Thread> threads = new ArrayList<>();
 		for (int i = 0; i < 5000; i++) {
 			//System.out.println(i);
			threads.add(new Thread(new Runnable() {
				@Override
				public synchronized void run() {
					long key = getNextId();
					Integer val = map.get(key);
					if(val!=null) {
						System.out.println(key);
					}
					map.put(key,1);
				}
			}, "t" + i));
			cdl.countDown();
		}
		cdl.await();
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			thread.join();
		}
		Thread.sleep(1000);
		System.out.println("map:"+map.size());
		long end = System.currentTimeMillis();
	}
	
	
	private static void test1() throws InterruptedException {
		long start = System.currentTimeMillis();
		Map<Long,Integer> map =new ConcurrentHashMap<>();
		CountDownLatch cdl = new CountDownLatch(5000);
		SnowFlake snowFlake = new SnowFlake(2, 3);
		List<Thread> threads = new ArrayList<>();
 		for (int i = 0; i < 5000; i++) {
			threads.add(new Thread(new Runnable() {
				@Override
				public void run() {
					long key = snowFlake.nextId();
					System.out.println(key);
					map.put(key,1);
				}
			}, "t" + i));
			cdl.countDown();
		}
		cdl.await();
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			thread.join();
		}
		System.out.println(map.size());
		long end = System.currentTimeMillis();
		System.out.println("end-start:"+(end-start));
	}
}
