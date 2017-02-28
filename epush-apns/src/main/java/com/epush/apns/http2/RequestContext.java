package com.epush.apns.http2;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by G2Y on 2017/2/28.
 */
public class RequestContext<R, Q> implements TimerTask, Http2Callback<Q> {


    private final long startTime = System.currentTimeMillis();
    final AtomicBoolean cancelled = new AtomicBoolean(false);
    final int readTimeout;
    private long endTime = startTime;
    private Http2Callback<Q> callback;
    R request;
    String host;

    public RequestContext(R request, Http2Callback<Q> callback, int readTimeout) {
        this.callback = callback;
        this.request = request;
        this.readTimeout = readTimeout;
    }

    public boolean tryDone() {
        return cancelled.compareAndSet(false, true);
    }


    public void onResponse(Q response) {
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
}
