package com.epush.apns.exception;

/**
 * APNS 异常类
 * 
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ApnsException extends RuntimeException {

	/**
	 * 
	 */
	public ApnsException() {
		super();
	}

	/**
	 * 
	 * @param msg
	 */
	public ApnsException(String msg) {
		super(msg);
	}

	/**
	 * 
	 * @param t
	 */
	public ApnsException(Throwable t) {
		super(t);
	}

	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public ApnsException(String msg, Throwable t) {
		super(msg, t);
	}
}
