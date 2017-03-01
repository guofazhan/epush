package com.epush.apns.http2;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by G2Y on 2017/2/28.
 */
public class RequestContext<T> implements TimerTask, Http2Callback<T> {

	private final long startTime = System.currentTimeMillis();
	final AtomicBoolean cancelled = new AtomicBoolean(false);
	final int readTimeout;
	private long endTime = startTime;
	private Http2Callback<T> callback;
	private Http2Request<T> request;
	Host host;

	public RequestContext(Http2Request<T> request, Http2Callback<T> callback,
			int readTimeout, Host host) {
		this.callback = callback;
		this.request = request;
		this.readTimeout = readTimeout;
		this.host = host;
	}

	public boolean tryDone() {
		return cancelled.compareAndSet(false, true);
	}

	public void onResponse(Http2Response<T> response) {
		callback.onResponse(response);
		endTime = System.currentTimeMillis();
		destroy();
	}

	public void onFailure(int statusCode, String reasonPhrase) {
		callback.onFailure(statusCode, reasonPhrase);
		endTime = System.currentTimeMillis();
		destroy();
	}

	public void onException(Throwable throwable) {
		callback.onException(throwable);
		endTime = System.currentTimeMillis();
		destroy();
	}

	public void onTimeout() {
		callback.onTimeout();
		endTime = System.currentTimeMillis();
		destroy();
	}

	public void run(Timeout timeout) throws Exception {
		if (tryDone()) {
			if (callback != null) {
				callback.onTimeout();
			}
		}
	}

	private void destroy() {
		request = null;
		callback = null;
	}

	public Http2Request getRequest() {
		return request;
	}

	public void setRequest(Http2Request request) {
		this.request = request;
	}
}
