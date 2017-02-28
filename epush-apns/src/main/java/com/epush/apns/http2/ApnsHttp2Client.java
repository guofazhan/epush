package com.epush.apns.http2;

import com.epush.apns.http2.proxy.ProxyHandlerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * ApnsHttp2Client
 * 
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ApnsHttp2Client extends Http2Client {

	private static final Logger log = LoggerFactory
			.getLogger(ApnsHttp2Client.class);

	// milliseconds
	static final int PING_IDLE_TIME_MILLIS = 60_000;

	private volatile ProxyHandlerFactory proxyHandlerFactory;

	private int writeBufferWaterMark;

	private int timeoutMillis;

	private long writeTimeoutMillis;

	private SslContext sslContext;

	private boolean useTokenAuthentication;

	private volatile ChannelPromise connectionReadyPromise;
	private volatile ChannelPromise reconnectionPromise;

	@Override
	protected void initPipeline(Channel ch) {
		final ChannelPipeline pipeline = ch.pipeline();
		if (this.proxyHandlerFactory != null) {
			pipeline.addFirst(this.proxyHandlerFactory.buildProxyHandler());
		}

		if (this.writeTimeoutMillis > 0) {
			pipeline.addLast(new WriteTimeoutHandler(this.writeTimeoutMillis,
					TimeUnit.MILLISECONDS));
		}

		if (null != this.sslContext) {
			pipeline.addLast(sslContext.newHandler(ch.alloc()));
		}

		pipeline.addLast(new ApplicationProtocolNegotiationHandler("") {
			@Override
			protected void configurePipeline(
					final ChannelHandlerContext context,
					final String protocol) {
				if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
					ApnsHttpHandler httpHandler = new ApnsHttpHandlerBuilder()
							.server(false).setHttp2Client(ApnsHttp2Client.this)
							.setAuthority(((InetSocketAddress) context.channel()
									.remoteAddress()).getHostName())
							.useTokenAuthentication(
									ApnsHttp2Client.this.useTokenAuthentication)
							.encoderEnforceMaxConcurrentStreams(true).build();

					// if (this.gracefulShutdownTimeoutMillis != null) {
					// httpHandler.gracefulShutdownTimeoutMillis(
					// .this.gracefulShutdownTimeoutMillis);
					// }

					context.pipeline().addLast(new IdleStateHandler(0, 0,
							PING_IDLE_TIME_MILLIS, TimeUnit.MILLISECONDS));
					context.pipeline().addLast(httpHandler);

					// Add this to the end of the queue so any events enqueued
					// by the client handler happen
					// before we declare victory.
					context.channel().eventLoop().submit(new Runnable() {
						@Override
						public void run() {
							final ChannelPromise connectionReadyPromise = ApnsHttp2Client.this.connectionReadyPromise;

							if (connectionReadyPromise != null) {
								connectionReadyPromise.trySuccess();
							}
						}
					});
				} else {
					log.error("Unexpected protocol: {}", protocol);
					context.close();
				}
			}

			@Override
			protected void handshakeFailure(final ChannelHandlerContext context,
					final Throwable cause) throws Exception {
				final ChannelPromise connectionReadyPromise = ApnsHttp2Client.this.connectionReadyPromise;

				if (connectionReadyPromise != null) {
					connectionReadyPromise.tryFailure(cause);
				}

				super.handshakeFailure(context, cause);
			}
		});
	}

	@Override
	protected void initOptions(Bootstrap b) {

	}
}
