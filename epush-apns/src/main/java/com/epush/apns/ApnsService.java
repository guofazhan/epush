package com.epush.apns;

import com.epush.apns.exception.ApnsException;

import java.util.Collection;

/**
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public final class ApnsService
		implements Push<PushNotificationResponse, ApnsPushNotification> {

	/**
	 * http2客户端
	 */
	private final ApnsHttp2Client http2Client;

	protected ApnsService(ApnsHttp2Client http2Client) {
		this.http2Client = http2Client;
	}

	/**
	 * 单个推送
	 *
	 * @param apnsPushNotification
	 * @return
	 * @throws ApnsException
	 */
	@Override
	public PushNotificationResponse push(
			ApnsPushNotification apnsPushNotification) throws ApnsException {
		return http2Client.push(apnsPushNotification);
	}

	/**
	 * 批量推送
	 *
	 * @param list
	 * @return
	 * @throws ApnsException
	 */
	@Override
	public Collection<PushNotificationResponse> push(
			Collection<ApnsPushNotification> list) throws ApnsException {
		return http2Client.push(list);
	}
}
