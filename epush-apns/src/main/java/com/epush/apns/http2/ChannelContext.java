package com.epush.apns.http2;

import io.netty.util.AttributeKey;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/3/7]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ChannelContext {

	/**
	 *
	 */
	public static final AttributeKey<Host> hostKey = AttributeKey
			.newInstance("host");

	/**
	 *
	 */
	public static final AttributeKey<Integer> streamIdKey = AttributeKey
			.newInstance("streamId");

	/**
	 *
	 */
	public static final AttributeKey<Http2SettingsHandler> settingHandlerKey = AttributeKey
			.newInstance("settingHandler");

	/**
	 *
	 */
	public static final AttributeKey<HttpResponseHandler> responseHandlerKey = AttributeKey
			.newInstance("responseHandler");
}
