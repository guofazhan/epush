package com.epush.apns.exception;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class InvalidSSLConfig extends ApnsException {

	private static final long serialVersionUID = -7283168775864517167L;

	public InvalidSSLConfig() {
		super();
	}

	public InvalidSSLConfig(String message) {
		super(message);
	}

	public InvalidSSLConfig(Throwable t) {
		super(t);
	}

	public InvalidSSLConfig(String m, Throwable c) {
		super(m, c);
	}
}
