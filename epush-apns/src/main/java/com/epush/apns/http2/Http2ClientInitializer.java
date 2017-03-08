package com.epush.apns.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;

import static io.netty.handler.logging.LogLevel.INFO;

/**
 * http2初始化
 */
public abstract class Http2ClientInitializer
		extends ChannelInitializer<SocketChannel> {

	private static final Http2FrameLogger logger = new Http2FrameLogger(INFO,
			Http2ClientInitializer.class);
	private final SslContext sslCtx;
	private final int maxContentLength;

	public Http2ClientInitializer(SslContext sslCtx, int maxContentLength) {
		this.sslCtx = sslCtx;
		this.maxContentLength = maxContentLength;
	}

	@Override
	protected void initChannel(SocketChannel socketChannel) throws Exception {
		configure(socketChannel);
		if (sslCtx != null) {
			ChannelPipeline pipeline = socketChannel.pipeline();
			pipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
			pipeline.addLast(new ApplicationProtocolNegotiationHandler("") {
				@Override
				protected void configurePipeline(ChannelHandlerContext ctx,
						String protocol) {
					if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
						ChannelPipeline p = ctx.pipeline();
						p.addLast(buildConnHandler());
						configureEndOfPipeline(p);
						return;
					}
					ctx.close();
					throw new IllegalStateException(
							"unknown protocol: " + protocol);
				}
			});

		} else {
			throw new IllegalStateException("unknown SslContext: " + sslCtx);
		}

	}

	/**
	 * 构建一个HttpToHttp2ConnectionHandler
	 * 
	 * @return
	 */
	protected HttpToHttp2ConnectionHandler buildConnHandler() {
		final Http2Connection connection = new DefaultHttp2Connection(false);
		return new HttpToHttp2ConnectionHandlerBuilder()
				.frameListener(
						new DelegatingDecompressorFrameListener(connection,
								new InboundHttp2ToHttpAdapterBuilder(connection)
										.maxContentLength(maxContentLength)
										.propagateSettings(true).build()))
				.frameLogger(logger).connection(connection).build();
	}



	/**
	 * 
	 * @param pipeline
	 */
	protected void configureEndOfPipeline(ChannelPipeline pipeline) {
		pipeline.addLast(pipeline.channel().attr(ChannelContext.settingHandlerKey).get(),
				pipeline.channel().attr(ChannelContext.responseHandlerKey).get());
	}

	/**
	 * 初始化配置信息 对外暴露子类扩展
	 * 
	 * @param channel
	 */
	public abstract void configure(SocketChannel channel);

}
