package com.reyco.lock.model;

import java.io.Serializable;

/**
*@author reyco
*@date  2021年3月19日---下午8:36:26
*<pre>
*
*<pre> 
*/
public class Product implements Serializable {
	private Integer id;
	private String price;
	private Integer stock;
	private Integer oldStock;
	private Integer newStock;
	private Integer qty;
	private String desc;
	private String gmtShelf;
	private String gmtCreate;
	private String gmtModified;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public Integer getStock() {
		return stock;
	}
	public void setStock(Integer stock) {
		this.stock = stock;
	}
	public Integer getOldStock() {
		return oldStock;
	}
	public void setOldStock(Integer oldStock) {
		this.oldStock = oldStock;
	}
	public Integer getNewStock() {
		return newStock;
	}
	public void setNewStock(Integer newStock) {
		this.newStock = newStock;
	}
	public Integer getQty() {
		return qty;
	}
	public void setQty(Integer qty) {
		this.qty = qty;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getGmtShelf() {
		return gmtShelf;
	}
	public void setGmtShelf(String gmtShelf) {
		this.gmtShelf = gmtShelf;
	}
	public String getGmtCreate() {
		return gmtCreate;
	}
	public void setGmtCreate(String gmtCreate) {
		this.gmtCreate = gmtCreate;
	}
	public String getGmtModified() {
		return gmtModified;
	}
	public void setGmtModified(String gmtModified) {
		this.gmtModified = gmtModified;
	}
}
