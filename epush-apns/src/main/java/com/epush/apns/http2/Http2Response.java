package com.epush.apns.http2;

import java.util.Date;

/**
 * 请求返回
 * 
 * @author guofazhan
 * @version [版本号, 2017/3/1]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class Http2Response<T> {
	/**
	 * 通知信息
	 */
	private final T data;

	/**
	 * 
	 */
	private final boolean success;
	/**
	 * 
	 */
	private final String rejectionReason;
	/**
	 * 
	 */
	private final Date tokenExpirationTimestamp;

	public Http2Response(final T data, final boolean success,
			final String rejectionReason, final Date tokenExpirationTimestamp) {
		this.data = data;
		this.success = success;
		this.rejectionReason = rejectionReason;
		this.tokenExpirationTimestamp = tokenExpirationTimestamp;
	}

	public T getData() {
		return data;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public Date getTokenExpirationTimestamp() {
		return tokenExpirationTimestamp;
	}
}
