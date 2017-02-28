package com.epush.apns.http2;

import com.epush.apns.http2.exception.Http2Exception;

/**
 * Created by G2Y on 2017/2/28.
 */
public interface Client {

    void init() throws Http2Exception;

    /**
     * @param context
     * @throws Exception
     */
    void request(RequestContext context) throws Http2Exception;
}
