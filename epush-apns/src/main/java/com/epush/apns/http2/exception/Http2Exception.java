package com.epush.apns.http2.exception;

import com.epush.apns.exception.ApnsException;

/**
 * Created by G2Y on 2017/2/28.
 */
public class Http2Exception extends ApnsException {

    private static final long serialVersionUID = -7283168775864517167L;

    public Http2Exception() {
        super();
    }

    public Http2Exception(String message) {
        super(message);
    }

    public Http2Exception(Throwable t) {
        super(t);
    }

    public Http2Exception(String m, Throwable c) {
        super(m, c);
    }
}
