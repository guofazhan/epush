package com.epush.apns.http2;

import com.epush.apns.http2.exception.Http2Exception;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http2.Http2Headers;

import java.nio.charset.StandardCharsets;

/**
 * Created by G2Y on 2017/3/3.
 */
public class Http2Response<T> {

    /**
     *
     */
    private T request;

    /**
     *
     */
    private Http2Headers headers2;

    /**
     *
     */
    private HttpHeaders headers;

    /**
     *
     */
    private Http2Exception exception;

    public Http2Response() {
    }

    public Http2Response(Http2Headers headers2) {
        this.headers2 = headers2;
    }

    public Http2Response(HttpHeaders headers) {
        this.headers = headers;
    }

    public Http2Response(T request, HttpHeaders headers) {
        this.headers = headers;
        this.request = request;
    }

    public T getRequest() {
        return request;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setRequest(T request) {
        this.request = request;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public Http2Exception getException() {
        return exception;
    }

    public void setException(Http2Exception exception) {
        this.exception = exception;
    }

    public static Http2Response toHttp2Response(FullHttpResponse response) {

        System.out.println("FullHttpResponse:" + response);
        return new Http2Response<String>(response.content().toString(StandardCharsets.UTF_8), response.headers());
    }

    public static Http2Response toHttp2Response(Http2Exception exception) {
        Http2Response response = new Http2Response<String>(exception.getMessage(), null);
        response.setException(exception);
        return response;
    }

    @Override
    public String toString() {
        return "Http2Response{" +
                "request=" + request +
                ", headers=" + headers +
                ", exception=" + exception +
                '}';
    }
}
