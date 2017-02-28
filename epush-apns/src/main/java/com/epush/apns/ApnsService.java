package com.epush.apns;

import com.epush.apns.authentication.P12;
import com.epush.apns.authentication.P8;
import com.epush.apns.http2.proxy.ProxyHandlerFactory;
import io.netty.handler.ssl.SslProvider;

/**
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public final class ApnsService {

	/**
	 * 环境信息
	 */
	private String environment;

	/**
	 * 代理信息
	 */
	private ProxyHandlerFactory proxyHandlerFactory;

	/**
	 * ssl信息
	 */
	private SslProvider sslProvider;

	/**
	 *
	 */
	private P12 p12;

	private P8 p8;

	private boolean useTokenAuthentication;


}
