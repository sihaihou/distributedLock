package com.reyco.lock.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.reyco.lock.dao.ProductDao;
import com.reyco.lock.model.Product;

/**
*@author reyco
*@date  2021年3月19日---下午10:58:14
*<pre>
*
*<pre> 
*/
@Service
public class ProductService {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ProductDao productDao;
	
	@Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.READ_COMMITTED )
	public Boolean updateStock(Product product) {
		int time = 1; 
		while(time<=3) {
			logger.debug("第"+time+"次");
			time++;
			Product selectProduct = null;
			selectProduct = productDao.get(product.getId());
			if(selectProduct.getStock()>=product.getQty()) {
				product.setOldStock(selectProduct.getStock());
				product.setNewStock(selectProduct.getStock()-product.getQty());
				Integer count = productDao.update(product);
				if(count==1) {
					logger.debug("扣减库存成功：qty:"+product.getQty());
					return true;
				}
			}else {
				logger.debug("库存不足");
				return false;
			}
		}
		logger.debug("扣减库存失败");
		return false;
	}
	
}
