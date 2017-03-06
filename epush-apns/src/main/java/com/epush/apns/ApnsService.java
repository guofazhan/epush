package com.epush.apns;

import com.epush.apns.authentication.P8;
import com.epush.apns.exception.ApnsException;
import com.epush.apns.http2.Host;
import com.epush.apns.http2.Http2Client;
import com.epush.apns.http2.Http2Request;
import com.epush.apns.http2.Http2Response;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;

import java.security.SignatureException;

/**
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public final class ApnsService implements
		Push<PushNotificationResponse<ApnsPushNotification>, ApnsPushNotification> {

	/**
	 * 环境信息
	 */
	private String environment;

	/**
	 * P8信息
	 */
	private P8 p8;

	/**
	 * 请求客户端
	 */
	private Http2Client client;

	/**
	 *
	 */
	protected ApnsService() {
	}

	public ApnsService setEnvironment(String environment) {
		this.environment = environment;
		return this;
	}

	public P8 getP8() {
		return p8;
	}

	public ApnsService setP8(P8 p8) {
		this.p8 = p8;
		return this;
	}

	public Http2Client getClient() {
		return client;
	}

	public ApnsService setClient(Http2Client client) {
		this.client = client;
		return this;
	}

	/**
	 * 单个推送
	 *
	 * @param pushNotification
	 * @return
	 * @throws ApnsException
	 */
	@Override
	public PushNotificationResponse<ApnsPushNotification> push(
			ApnsPushNotification pushNotification) throws ApnsException {
		PushNotificationResponse<ApnsPushNotification> response = null;
		try {
			Http2Response http2Response = client
					.request(buildHttp2Request(pushNotification));

			if (null != http2Response) {
				response = parseHttp2Response(http2Response);
			} else {
				response = new PushNotificationResponse<ApnsPushNotification>();
				response.setPushNotification(pushNotification);
				response.setSuccess(false);
				response.setRejectionReason("HTTP2 Response Is Empty");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response = new PushNotificationResponse<ApnsPushNotification>();
			response.setPushNotification(pushNotification);
			response.setSuccess(false);
			response.setRejectionReason(e.getMessage());
		}

		return response;
	}

	protected Http2Request buildHttp2Request(
			ApnsPushNotification apnsPushNotification)
			throws SignatureException {
		final HttpHeaders httpHeaders = new DefaultHttpHeaders()
				.addInt(ApnsConfigure.APNS_EXPIRATION_HEADER, 0);
		if (this.p8 != null) {
			httpHeaders.add(ApnsConfigure.APNS_AUTHORIZATION_HEADER,
					"bearer " + this.p8
							.getAuthenticationTokenSupplierForTopic(
									apnsPushNotification.getTopic())
							.getToken());
		} else {
		}

		if (apnsPushNotification.getCollapseId() != null) {
			httpHeaders.add(ApnsConfigure.APNS_COLLAPSE_ID_HEADER,
					apnsPushNotification.getCollapseId());
		}

		if (apnsPushNotification.getPriority() != null) {
			httpHeaders.addInt(ApnsConfigure.APNS_PRIORITY_HEADER,
					apnsPushNotification.getPriority().getCode());
		}

		if (apnsPushNotification.getTopic() != null) {
			httpHeaders.add(ApnsConfigure.APNS_TOPIC_HEADER,
					apnsPushNotification.getTopic());
		}

		ApnsConfigure.Environment environment = ApnsConfigure.Environment
				.getEnvironment(this.environment);
		final Host host = new Host(environment.getHost(),
				environment.getPort());
		// 构建一个HTTP2请求
		return new Http2Request(apnsPushNotification.getPayload(), httpHeaders,
				host, ApnsConfigure.APNS_PATH_PREFIX
						+ apnsPushNotification.getToken());
	}

	protected PushNotificationResponse<ApnsPushNotification> parseHttp2Response(
			Http2Response http2Response) {

		System.out.println(http2Response);
		return null;
	}

}
