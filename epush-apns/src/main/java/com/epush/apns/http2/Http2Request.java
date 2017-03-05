package com.epush.apns.http2;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * Created by G2Y on 2017/3/3.
 */
public class Http2Request<T> {

    /**
     *
     */
    private final T request;

    /**
     *
     */
    private final Http2Headers headers;

    private final HttpHeaders httpHeaders;

    /**
     *
     */
    private final Host host;


    public Http2Request(T request, Http2Headers headers, Host host, HttpHeaders httpHeaders) {
        this.headers = headers;
        this.host = host;
        this.request = request;
        this.httpHeaders = httpHeaders;
    }


    public T getRequest() {
        return request;
    }

    public Http2Headers getHeaders() {
        return headers;
    }

    public Host getHost() {
        return host;
    }

    /**
     * Http2Request 转化为FullHttpRequest
     *
     * @return
     */
    public FullHttpRequest toFullHttpRequest() {
        Objects.requireNonNull(headers, "headers must be set before building an FullHttpRequest.");
        String reqStr = (String) this.request;
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, headers.path().toString(), wrappedBuffer(reqStr.getBytes(CharsetUtil.UTF_8)));
        HttpScheme scheme = HttpScheme.HTTPS;
        AsciiString hostName = new AsciiString(host.getHost() + ':' + host.getPort());
        request.headers().add(HttpHeaderNames.HOST, hostName);
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
        request.headers().setAll(httpHeaders);
        return request;
    }

}
