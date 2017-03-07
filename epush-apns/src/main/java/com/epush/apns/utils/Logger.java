package com.epush.apns.utils;

import org.slf4j.LoggerFactory;

/**
 * 日志工具
 *
 * @author guofazhan
 * @version [版本号, 2017/3/7]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface Logger {

	/**
	 * HTTP2 部分日志
	 */
	org.slf4j.Logger HTTP2 = LoggerFactory.getLogger("Http2");

	/**
	 * APNS 部分日志
	 */
	org.slf4j.Logger APNS = LoggerFactory.getLogger("Apns");
}
