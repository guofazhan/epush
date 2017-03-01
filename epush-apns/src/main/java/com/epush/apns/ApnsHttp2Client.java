package com.epush.apns;

import com.epush.apns.authentication.P8;
import com.epush.apns.exception.ApnsException;
import com.epush.apns.http2.*;
import com.epush.apns.http2.proxy.ProxyHandlerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author guofazhan
 * @version [版本号, 2017/3/1]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ApnsHttp2Client extends NettyHttp2Client
		implements Push<PushNotificationResponse, ApnsPushNotification> {

	private static final Logger log = LoggerFactory
			.getLogger(ApnsHttp2Client.class);

	private long connectionTimeout;

	private long writeTimeout;

	private long gracefulShutdownTimeout;

	/**
	 * 代理信息
	 */
	private ProxyHandlerFactory proxyHandlerFactory;

	private SslContext sslContext;

	/**
	 *
	 */
	private P8 p8;

	private boolean useTokenAuthentication;

	/**
	 * 环境信息
	 */
	private String environment;

	public long getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public long getWriteTimeout() {
		return writeTimeout;
	}

	public void setWriteTimeout(long writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

	public long getGracefulShutdownTimeout() {
		return gracefulShutdownTimeout;
	}

	public void setGracefulShutdownTimeout(long gracefulShutdownTimeout) {
		this.gracefulShutdownTimeout = gracefulShutdownTimeout;
	}

	public ProxyHandlerFactory getProxyHandlerFactory() {
		return proxyHandlerFactory;
	}

	public void setProxyHandlerFactory(
			ProxyHandlerFactory proxyHandlerFactory) {
		this.proxyHandlerFactory = proxyHandlerFactory;
	}

	public SslContext getSslContext() {
		return sslContext;
	}

	public void setSslContext(SslContext sslContext) {
		this.sslContext = sslContext;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public P8 getP8() {
		return p8;
	}

	public void setP8(P8 p8) {
		this.p8 = p8;
	}

	public boolean isUseTokenAuthentication() {
		return useTokenAuthentication;
	}

	public void setUseTokenAuthentication(boolean useTokenAuthentication) {
		this.useTokenAuthentication = useTokenAuthentication;
	}

	/**
	 * @return
	 */
	@Override
	protected Http2ClientInitializer getHttp2ClientInitializer() {
		return new ApnsHttp2ClientInitializer(sslContext, 0, this);
	}

	/**
	 * 配置初始化
	 *
	 * @param b
	 */
	@Override
	protected void initOptions(Bootstrap b) {

	}

	/**
	 * 单个推送
	 *
	 * @param apnsPushNotification
	 * @return
	 * @throws ApnsException
	 */
	@Override
	public PushNotificationResponse push(
			ApnsPushNotification apnsPushNotification) throws ApnsException {
		request(builderRequestContext(apnsPushNotification));
		return null;
	}

	/**
	 * 批量推送
	 *
	 * @param list
	 * @return
	 * @throws ApnsException
	 */
	@Override
	public Collection<PushNotificationResponse> push(
			Collection<ApnsPushNotification> list) throws ApnsException {
		return null;
	}

	protected RequestContext<ApnsPushNotification> builderRequestContext(
			ApnsPushNotification apnsPushNotification) {
		ApnsConfigure.Environment ev = ApnsConfigure.Environment
				.getEnvironment(environment);
		Host host = new Host(ev.getHost(), ev.getPort());
		RequestContext<ApnsPushNotification> requestContext = new RequestContext(
				builder(apnsPushNotification),
				new Http2Callback<ApnsPushNotification>() {
					@Override
					public void onResponse(
							Http2Response<ApnsPushNotification> response) {
					}

					@Override
					public void onFailure(int statusCode, String reasonPhrase) {
					}

					@Override
					public void onException(Throwable throwable) {
					}

					@Override
					public void onTimeout() {
					}
				}, 0, host);

		return requestContext;
	}

	protected Http2Request builder(ApnsPushNotification apnsPushNotification) {
		Http2Request<ApnsPushNotification> request = new Http2Request();
		request.setData(apnsPushNotification);
		return request;
	}

	protected PushNotificationResponse builder(
			Http2Response<ApnsPushNotification> response) {
		return new PushNotificationResponse(response);
	}

	static class ApnsHttp2ClientInitializer extends Http2ClientInitializer {
		// milliseconds
		static final int PING_IDLE_TIME_MILLIS = 60_000;

		private final ApnsHttp2Client client;

		public ApnsHttp2ClientInitializer(SslContext sslCtx,
				int maxContentLength, ApnsHttp2Client client) {
			super(sslCtx, maxContentLength);
			this.client = client;
		}

		@Override
		protected void initChannel(SocketChannel channel) throws Exception {
			final ChannelPipeline pipeline = channel.pipeline();

			final ProxyHandlerFactory proxyHandlerFactory = client.proxyHandlerFactory;

			if (proxyHandlerFactory != null) {
				pipeline.addFirst(proxyHandlerFactory.buildProxyHandler());
			}

			if (client.writeTimeout > 0) {
				pipeline.addLast(new WriteTimeoutHandler(client.writeTimeout,
						TimeUnit.MILLISECONDS));
			}
			pipeline.addLast(getSslCtx().newHandler(channel.alloc()));
			pipeline.addLast(new ApplicationProtocolNegotiationHandler("") {
				@Override
				protected void configurePipeline(
						final ChannelHandlerContext context,
						final String protocol) {
					if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {

						Http2ClientHandler clientHandler = new Http2ClientHandlerBuilder()
								.setAuthority(
										((InetSocketAddress) context.channel()
												.remoteAddress()).getHostName())
								.setHttp2Client(client)
								.useTokenAuthentication(
										client.isUseTokenAuthentication())
								.build();
						synchronized (client.getBootstrap()) {
							if (client.getGracefulShutdownTimeout() != 0) {
								clientHandler.gracefulShutdownTimeoutMillis(
										client.getGracefulShutdownTimeout());
							}
						}

						context.pipeline().addLast(new IdleStateHandler(0, 0,
								PING_IDLE_TIME_MILLIS, TimeUnit.MILLISECONDS));
						context.pipeline().addLast(clientHandler);
					} else {
						log.error("Unexpected protocol: {}", protocol);
						context.close();
					}
				}

				@Override
				protected void handshakeFailure(
						final ChannelHandlerContext context,
						final Throwable cause) throws Exception {
					super.handshakeFailure(context, cause);
				}
			});
		}
	}
}
