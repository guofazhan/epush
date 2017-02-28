package com.epush.apns.http2.proxy;

import io.netty.handler.proxy.ProxyHandler;

/**
 * 代理
 * 
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface ProxyHandlerFactory {

	/**
	 * 构建代理Handler
	 * 
	 * @return
	 */
	ProxyHandler buildProxyHandler();
}
