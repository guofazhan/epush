package com.epush.apns.http2;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/3/1]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class Http2ClientInitializer extends ChannelInitializer<SocketChannel> {

	private static final Logger logger = LoggerFactory
			.getLogger(Http2ClientInitializer.class);

	private final SslContext sslCtx;
	private final int maxContentLength;

	public Http2ClientInitializer(SslContext sslCtx, int maxContentLength) {
		this.sslCtx = sslCtx;
		this.maxContentLength = maxContentLength;
	}

	public int getMaxContentLength() {
		return maxContentLength;
	}

	public SslContext getSslCtx() {
		return sslCtx;
	}

	@Override
	protected void initChannel(SocketChannel socketChannel) throws Exception {
	}

}
