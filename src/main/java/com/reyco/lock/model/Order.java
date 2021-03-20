package com.reyco.lock.model;

import java.io.Serializable;

/**
*@author reyco
*@date  2021年3月19日---下午8:31:14
*<pre>
*
*<pre> 
*/
public class Order implements Serializable{
	private Integer id;
	private String no;
	private Integer state;
	private String desc;
	private String gmtExpire;
	private String gmtCreate;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getGmtExpire() {
		return gmtExpire;
	}
	public void setGmtExpire(String gmtExpire) {
		this.gmtExpire = gmtExpire;
	}
	public String getGmtCreate() {
		return gmtCreate;
	}
	public void setGmtCreate(String gmtCreate) {
		this.gmtCreate = gmtCreate;
	}
}
