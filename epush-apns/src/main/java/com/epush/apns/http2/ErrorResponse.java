package com.epush.apns.http2;

import java.util.Date;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/3/1]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ErrorResponse {

	private final String reason;
	private final Date timestamp;

	public ErrorResponse(final String reason, final Date timestamp) {
		this.reason = reason;
		this.timestamp = timestamp;
	}

	public String getReason() {
		return reason;
	}

	public Date getTimestamp() {
		return timestamp;
	}
}
