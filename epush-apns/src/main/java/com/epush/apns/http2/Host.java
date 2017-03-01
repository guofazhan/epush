package com.epush.apns.http2;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/3/1]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public final class Host {

	/**
	 * ip
	 */
	private String host;

	/**
	 *
	 */
	private int port;

	public Host() {
	}

	public Host(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public Host setHost(String host) {
		this.host = host;
		return this;
	}

	public int getPort() {
		return port;
	}

	public Host setPort(int port) {
		this.port = port;
		return this;
	}
}
