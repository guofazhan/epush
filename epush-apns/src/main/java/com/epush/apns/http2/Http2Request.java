package com.epush.apns.http2;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.util.AsciiString;

import java.util.HashMap;
import java.util.Map;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/3/1]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class Http2Request<T> {

	private Http2Headers header;

	private T data;

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public Http2Headers getHeader() {
		return header;
	}

	public void setHeader(Http2Headers header) {
		this.header = header;
	}

	public static Http2HeadersBuilder newBuilder() {
		return new Http2HeadersBuilder();
	}

	static class Http2HeadersBuilder {
		private String authority;
		private String path;
		private Map<AsciiString, Object> params = new HashMap<>();

		public String getAuthority() {
			return authority;
		}

		public Http2HeadersBuilder setAuthority(String authority) {
			this.authority = authority;
			return this;
		}

		public String getPath() {
			return path;
		}

		public Http2HeadersBuilder setPath(String path) {
			this.path = path;
			return this;
		}

		public Map<AsciiString, Object> getParams() {
			return params;
		}

		public Http2HeadersBuilder setParams(Map<AsciiString, Object> params) {
			this.params = params;
			return this;
		}

		public Http2HeadersBuilder addParam(String name, int value) {
			params.put(new AsciiString(name), value);
			return this;
		}

		public Http2HeadersBuilder addParam(String name, String value) {
			params.put(new AsciiString(name), value);
			return this;
		}

		public Http2Headers build() {
			Http2Headers headers = new DefaultHttp2Headers()
					.method(HttpMethod.POST.asciiName());
			headers.authority(this.authority).path(this.path);
			for (AsciiString key : params.keySet()) {
				if (params.get(key) instanceof String) {
					headers.add(key, (String) params.get(key));
				} else {
					headers.addInt(key, (int) params.get(key));
				}
			}
			return headers;
		}

	}
}
