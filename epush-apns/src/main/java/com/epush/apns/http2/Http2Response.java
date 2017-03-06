package com.epush.apns.http2;

import com.epush.apns.http2.exception.Http2Exception;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;

import java.nio.charset.StandardCharsets;

/**
 * Created by G2Y on 2017/3/3.
 */
public class Http2Response {

	/**
	 *
	 */
	private String response;

	/**
	 *
	 */
	private HttpHeaders headers;

	/**
	 *
	 */
	private Http2Exception exception;

	protected Http2Response() {
	}

	protected Http2Response(HttpHeaders headers) {
		this.headers = headers;
	}

	protected Http2Response(String response, HttpHeaders headers) {
		this.headers = headers;
		this.response = response;
	}

	public HttpHeaders getHeaders() {
		return headers;
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
		return new Http2Response(
				response.content().toString(StandardCharsets.UTF_8),
				response.headers());
	}

	public static Http2Response toHttp2Response(Http2Exception exception) {
		Http2Response response = new Http2Response(exception.getMessage(),
				null);
		response.setException(exception);
		return response;
	}

	@Override
	public String toString() {
		return "Http2Response{" + "response=" + response + ", headers="
				+ headers + ", exception=" + exception + '}';
	}
}
