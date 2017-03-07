package com.epush.apns.http2;

import com.epush.apns.http2.exception.Http2Exception;
import io.netty.handler.codec.http.FullHttpResponse;


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
	private FullHttpResponse httpResponse;

	/**
	 *
	 */
	private Http2Exception exception;

	protected Http2Response() {
	}

	protected Http2Response(FullHttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}


	public FullHttpResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(FullHttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public Http2Exception getException() {
		return exception;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public void setException(Http2Exception exception) {
		this.exception = exception;
	}

	/**
	 *
	 * @param response
	 * @return
     */
	public static Http2Response toHttp2Response(FullHttpResponse response) {
		return new Http2Response(response);
	}

	/**
	 *
	 * @param exception
	 * @return
     */
	public static Http2Response toHttp2Response(Http2Exception exception) {
		Http2Response response = new Http2Response(null);
		response.setResponse(exception.getMessage());
		response.setException(exception);
		return response;
	}

	@Override
	public String toString() {
		return "Http2Response{" + "response=" + response + ", httpResponse="
				+ httpResponse + ", exception=" + exception + '}';
	}
}
