package com.reyco.lock.dao;

import org.apache.ibatis.annotations.Insert;

import com.reyco.lock.model.Order;

/**
*@author reyco
*@date  2021年3月19日---下午8:29:36
*<pre>
*
*<pre> 
*/
public interface OrderDao {
	
	@Insert("insert into `order`(`no`,`state`,`desc`,`gmtExpire`,`gmtCreate`) "
			+ "values (#{no},#{state},#{desc},#{gmtExpire},#{gmtCreate})")
	void saveOrder(Order order);
	
	
}
