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
public class PushNotificationResponse<T extends ApnsPushNotification> {

	/**
	 * 通知详情
	 */
	private T pushNotification;
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

	public T getPushNotification() {
		return pushNotification;
	}

	public PushNotificationResponse<? extends ApnsPushNotification> setPushNotification(
			T pushNotification) {
		this.pushNotification = pushNotification;
		return this;
	}

	public boolean isSuccess() {
		return success;
	}

	public PushNotificationResponse<? extends ApnsPushNotification> setSuccess(
			boolean success) {
		this.success = success;
		return this;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public PushNotificationResponse<? extends ApnsPushNotification> setRejectionReason(
			String rejectionReason) {
		this.rejectionReason = rejectionReason;
		return this;
	}

	public Date getTokenExpirationTimestamp() {
		return tokenExpirationTimestamp;
	}

	public PushNotificationResponse<? extends ApnsPushNotification> setTokenExpirationTimestamp(
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
