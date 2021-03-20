package com.reyco.lock.dao;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.reyco.lock.model.Product;

/**
*@author reyco
*@date  2021年3月19日---下午8:36:09
*<pre>
*
*<pre> 
*/
public interface ProductDao {
	
	@Select({"select * from product where id = #{id}"})
	Product get(Integer id);
	
	@Update({"update product set stock = #{newStock} where id = #{id} and stock=#{oldStock}"})
	Integer update(Product product);
}
