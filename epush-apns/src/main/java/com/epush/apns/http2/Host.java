package com.epush.apns.http2;

/**
 * 
 */
public class Host {

	/**
	 * IP
	 */
	private String host;

	/**
	 * 端口
	 */
	private int port;

	public Host(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "Host{" + "host='" + host + '\'' + ", port=" + port + '}';
	}
}
