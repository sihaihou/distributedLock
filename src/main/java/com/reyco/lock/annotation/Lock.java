package com.reyco.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
*@author reyco
*@date  2021年3月20日---上午11:08:43
*<pre>
*
*<pre> 
*/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Lock {
	/**
	 * 锁的名称
	 * @return
	 */
	String value() default "default";
	/**
	 * 过期时间/毫秒
	 * @return
	 */
	int expireTime() default 3000;
	/**
	 * 获取锁的超时时间：指定时间内没有获取到锁则丢弃,
	 * @return
	 */
	int timeout() default 3000;
}
