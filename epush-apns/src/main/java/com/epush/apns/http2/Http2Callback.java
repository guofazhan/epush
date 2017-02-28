package com.epush.apns.http2;

/**
 * Created by G2Y on 2017/2/28.
 */
public interface Http2Callback<Q> {

    void onResponse(Q response);

    void onFailure(int statusCode, String reasonPhrase);

    void onException(Throwable throwable);

    void onTimeout();
}
