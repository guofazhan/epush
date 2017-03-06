package com.epush.apns.http2;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

import java.util.Objects;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * Created by G2Y on 2017/3/3.
 */
public class Http2Request {

	/**
	 *
	 */
	private final String request;

	/**
	 * 请求地址
	 */
	private final String path;

	private final HttpHeaders headers;

	/**
	 *
	 */
	private final Host host;

	public Http2Request(String request, HttpHeaders headers, Host host,
			String path) {
		this.headers = headers;
		this.host = host;
		this.request = request;
		this.path = path;
	}

	public String getRequest() {
		return request;
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
		Objects.requireNonNull(headers,
				"headers must be set before building an FullHttpRequest.");
		String reqStr = (String) this.request;
		DefaultFullHttpRequest request = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_1, HttpMethod.POST, path,
				wrappedBuffer(reqStr.getBytes(CharsetUtil.UTF_8)));
		HttpScheme scheme = HttpScheme.HTTPS;
		AsciiString hostName = new AsciiString(
				host.getHost() + ':' + host.getPort());
		request.headers().add(HttpHeaderNames.HOST, hostName);
		request.headers().add(
				HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(),
				scheme.name());
		request.headers().add(HttpHeaderNames.ACCEPT_ENCODING,
				HttpHeaderValues.GZIP);
		request.headers().add(HttpHeaderNames.ACCEPT_ENCODING,
				HttpHeaderValues.DEFLATE);
		request.headers().setAll(headers);
		return request;
	}

}
