package com.epush.apns.http2;

import com.epush.apns.exception.ApnsException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/3/1]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class Http2ClientHandlerBuilder extends
		AbstractHttp2ConnectionHandlerBuilder<Http2ClientHandler, Http2ClientHandlerBuilder> {
	private static final Logger logger = LoggerFactory
			.getLogger(Http2ClientHandlerBuilder.class);

	private NettyHttp2Client client;
	private String authority;
	private boolean useTokenAuthentication;

	private static final Gson gson = new GsonBuilder().serializeNulls()
			.disableHtmlEscaping().create();

	public Http2ClientHandlerBuilder setHttp2Client(
			final NettyHttp2Client client) {
		this.client = client;
		return this;
	}

	public Http2ClientHandlerBuilder setAuthority(final String authority) {
		this.authority = authority;
		return this;
	}

	public Http2ClientHandlerBuilder useTokenAuthentication(
			final boolean useTokenAuthentication) {
		this.useTokenAuthentication = useTokenAuthentication;
		return this;
	}

	@Override
	protected Http2ClientHandler build(
			Http2ConnectionDecoder http2ConnectionDecoder,
			Http2ConnectionEncoder http2ConnectionEncoder,
			Http2Settings http2Settings) throws Exception {
		Objects.requireNonNull(getAuthority(),
				"Authority must be set before building an ApnsClientHandler.");
		final Http2ClientHandler handler = new Http2ClientHandler(
				http2ConnectionDecoder, http2ConnectionEncoder, http2Settings,
				getHttp2Client(), getAuthority(), isUseTokenAuthentication());
		this.frameListener(new Http2ClientHandlerFrameAdapter());
		return handler;
	}

	public NettyHttp2Client getHttp2Client() {
		return client;
	}

	public String getAuthority() {
		return authority;
	}

	public boolean isUseTokenAuthentication() {
		return useTokenAuthentication;
	}

	@Override
	public Http2ClientHandler build() {
		return super.build();
	}

	class Http2ClientHandlerFrameAdapter extends Http2FrameAdapter {
		@Override
		public void onSettingsRead(final ChannelHandlerContext context,
				final Http2Settings settings) {
			logger.trace("Received settings from APNs gateway: {}", settings);
		}

		@Override
		public void onGoAwayRead(final ChannelHandlerContext context,
				final int lastStreamId, final long errorCode,
				final ByteBuf debugData) throws Http2Exception {
			logger.info("Received GOAWAY from APNs server: {}",
					debugData.toString(StandardCharsets.UTF_8));
		}

		@Override
		public void onPingAckRead(final ChannelHandlerContext context,
				final ByteBuf data) {
			// if (ApnsClientHandler.this.pingTimeoutFuture != null) {
			// logger.trace("Received reply to ping.");
			// ApnsClientHandler.this.pingTimeoutFuture.cancel(false);
			// } else {
			// logger.error(
			// "Received PING ACK, but no corresponding outbound PING found.");
			// }
		}

		@Override
		public void onHeadersRead(final ChannelHandlerContext context,
				final int streamId, final Http2Headers headers,
				final int padding, final boolean endOfStream)
				throws Http2Exception {
			logger.trace("Received headers from APNs gateway on stream {}: {}",
					streamId, headers);

			if (endOfStream) {
				final HttpResponseStatus status = HttpResponseStatus
						.parseLine(headers.status());
				final boolean success = HttpResponseStatus.OK.equals(status);

				if (!success) {
					logger.warn(
							"Gateway sent an end-of-stream HEADERS frame for an unsuccessful notification.");
				}

				RequestContext rcontext = context.channel()
						.attr(client.requestKey).get();

//				final ApnsPushNotification pushNotification = ApnsClientHandler.this.pushNotificationsByStreamId
//						.remove(streamId);
//				ApnsClientHandler.this.authenticationTokensByStreamId
//						.remove(streamId);

				if (HttpResponseStatus.INTERNAL_SERVER_ERROR.equals(status)) {
					rcontext.onFailure(503, "Service Unavailable");
				} else {
					// 创建返回响应，获取响应信息
					rcontext.onResponse(
							new Http2Response(rcontext.getRequest().getData(),
									success,
									null,
									null));
				}
			} else {

				//ApnsClientHandler.this.headersByStreamId.put(streamId, headers);
			}
		}

		@Override
		public int onDataRead(final ChannelHandlerContext context,
				final int streamId, final ByteBuf data, final int padding,
				final boolean endOfStream) throws Http2Exception {
			logger.trace("Received data from APNs gateway on stream {}: {}",
					streamId, data.toString(StandardCharsets.UTF_8));
			final int bytesProcessed = data.readableBytes() + padding;

			if (endOfStream) {
				RequestContext rcontext = context.channel()
						.attr(client.requestKey).get();
				Http2Request http2Request = rcontext.getRequest();
				Http2Headers headers = http2Request.getHeader();
				// final String authenticationToken =
				// ApnsHttpHandler.this.authenticationTokensByStreamId
				// .remove(streamId);
				HttpResponseStatus status = HttpResponseStatus
						.parseLine(headers.status());
				String responseBody = data.toString(StandardCharsets.UTF_8);

				if (HttpResponseStatus.INTERNAL_SERVER_ERROR.equals(status)) {
					rcontext.onException(new ApnsException(responseBody));
				} else {
					final ErrorResponse errorResponse = gson
							.fromJson(responseBody, ErrorResponse.class);
					// if (ApnsClient.EXPIRED_AUTH_TOKEN_REASON
					// .equals(errorResponse.getReason())) {
					// try {
					// ApnsClientHandler.this.apnsClient
					// .getAuthenticationTokenSupplierForTopic(
					// pushNotification.getTopic())
					// .invalidateToken(authenticationToken);
					// } catch (final NoKeyForTopicException e) {
					// logger.warn(
					// "Authentication token expired, but no key registered for
					// topic {}",
					// pushNotification.getTopic());
					// }
					// }

					// 创建返回响应，获取响应信息
					rcontext.onResponse(
							new Http2Response(http2Request.getData(),
									HttpResponseStatus.OK.equals(status),
									errorResponse.getReason(),
									errorResponse.getTimestamp()));
				}
			} else {
				logger.error(
						"Gateway sent a DATA frame that was not the end of a stream.");
			}

			return bytesProcessed;
		}

	}
}
