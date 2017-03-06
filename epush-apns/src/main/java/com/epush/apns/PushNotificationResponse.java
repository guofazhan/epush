package com.epush.apns;

import java.util.Date;

/**
 * 通知返回
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class PushNotificationResponse {

	/**
	 * 通知详情
	 */
	private ApnsPushNotification pushNotification;
	/**
	 * 成功表示
	 */
	private boolean success;
	/**
	 * 失败原因
	 */
	private String rejectionReason;
	/**
	 * 失效时间
	 */
	private Date tokenExpirationTimestamp;

	protected PushNotificationResponse() {
	}

	public static PushNotificationResponse build(
			ApnsPushNotification pushNotification) {
		return new PushNotificationResponse().setSuccess(true)
				.setPushNotification(pushNotification).setRejectionReason("")
				.setTokenExpirationTimestamp(null);
	}

	/**
	 *
	 * @param success
	 * @param rejectionReason
	 * @return
	 */
	public static PushNotificationResponse build(boolean success,
			String rejectionReason) {
		return new PushNotificationResponse().setSuccess(success)
				.setPushNotification(null).setRejectionReason(rejectionReason)
				.setTokenExpirationTimestamp(null);
	}

	/**
	 *
	 * @param pushNotification
	 * @param success
	 * @param rejectionReason
	 * @return
	 */
	public static PushNotificationResponse build(
			ApnsPushNotification pushNotification, boolean success,
			String rejectionReason) {
		return new PushNotificationResponse().setSuccess(success)
				.setPushNotification(pushNotification)
				.setRejectionReason(rejectionReason)
				.setTokenExpirationTimestamp(null);
	}

	/**
	 * 构建推送响应
	 *
	 * @param pushNotification
	 * @param success
	 * @param rejectionReason
	 * @param tokenExpirationTimestamp
	 * @return
	 */
	public static PushNotificationResponse build(
			ApnsPushNotification pushNotification, boolean success,
			String rejectionReason, Date tokenExpirationTimestamp) {
		return new PushNotificationResponse().setSuccess(success)
				.setPushNotification(pushNotification)
				.setRejectionReason(rejectionReason)
				.setTokenExpirationTimestamp(tokenExpirationTimestamp);
	}

	public ApnsPushNotification getPushNotification() {
		return pushNotification;
	}

	public PushNotificationResponse setPushNotification(
			ApnsPushNotification pushNotification) {
		this.pushNotification = pushNotification;
		return this;
	}

	public boolean isSuccess() {
		return success;
	}

	public PushNotificationResponse setSuccess(boolean success) {
		this.success = success;
		return this;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public PushNotificationResponse setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
		return this;
	}

	public Date getTokenExpirationTimestamp() {
		return tokenExpirationTimestamp;
	}

	public PushNotificationResponse setTokenExpirationTimestamp(
			Date tokenExpirationTimestamp) {
		this.tokenExpirationTimestamp = tokenExpirationTimestamp;
		return this;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PushNotificationResponse [pushNotification=");
		builder.append(this.pushNotification);
		builder.append(", success=");
		builder.append(this.success);
		builder.append(", rejectionReason=");
		builder.append(this.rejectionReason);
		builder.append(", tokenExpirationTimestamp=");
		builder.append(this.tokenExpirationTimestamp);
		builder.append("]");
		return builder.toString();
	}
}
