package com.reyco.lock.service.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.reyco.lock.annotation.Lock;
import com.reyco.lock.dao.OrderDao;
import com.reyco.lock.dao.ProductDao;
import com.reyco.lock.model.Order;
import com.reyco.lock.model.Product;
import com.reyco.lock.service.TestService;
import com.reyco.lock.utils.SnowFlake;

@Service("testService")
public class TestServiceImpl implements TestService{
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private OrderDao orderDao;
	
	@Autowired
	private ProductDao productDao;
	@Autowired
	private ProductService productService;
	
	public String test() {
		logger.info("执行目标方法test");
		return "";
	}
	/**
	 * mysql乐观锁测试
	 */
	@Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.READ_COMMITTED )
	public void testLock() {
		Order order = new Order();
		order.setNo(SnowFlake.getNextId().toString());
		order.setState(1);
		order.setDesc("remark");
		order.setGmtExpire("2021-03-19 20:53:00");
		order.setGmtCreate("2021-03-19 20:53:00");
		orderDao.saveOrder(order);
		//
		Product product = new Product();
		product.setId(1);
		product.setQty(1);
		Boolean flag = productService.updateStock(product);
		if(!flag) {
			 System.out.println(Thread.currentThread().getName()+"，失败");
			 throw new RuntimeException("库存扣减失败");
		}
		System.out.println(Thread.currentThread().getName()+"，成功");
	}
	
	/**
	 * redis锁测试
	 */
	@Lock
	@Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.READ_COMMITTED )
	public void testLock1() {
		Product product = productDao.get(1);
		if(product.getStock()<1) {
			System.out.println(Thread.currentThread().getName()+"，库存不足");
			return;
		}
		try {
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Order order = new Order();
		order.setNo(SnowFlake.getNextId().toString());
		order.setState(1);
		order.setDesc("remark");
		order.setGmtExpire("2021-03-19 20:53:00");
		order.setGmtCreate("2021-03-19 20:53:00");
		orderDao.saveOrder(order);
		//
		product.setOldStock(product.getStock());
		product.setNewStock(product.getStock()-1);
		productDao.update(product);
		System.out.println(Thread.currentThread().getName()+"，成功");
	}
	
	@Transactional
	public String test1() {
		logger.info("执行目标方法test1");
		TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
		return "";
	}
}
