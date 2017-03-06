package com.epush.apns.http2;

import com.epush.apns.http2.exception.Http2Exception;

/**
 * Created by G2Y on 2017/2/28.
 */
public interface Client {

	/**
	 * @throws Http2Exception
	 */
	void init() throws Http2Exception;

	/**
	 * @return
	 * @throws Http2Exception
	 */
	boolean start() throws Http2Exception;

	/**
	 * @param request
	 * @return
	 * @throws Http2Exception
	 */
	 Http2Response request(Http2Request request) throws Http2Exception;

	/**
	 * @return
	 * @throws Http2Exception
	 */
	boolean isRuning() throws Http2Exception;

	/**
	 * @return
	 * @throws Http2Exception
	 */
	boolean shutdown() throws Http2Exception;

}
