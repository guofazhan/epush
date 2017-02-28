package com.epush.apns.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameAdapter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;


/**
 * HttpHandlerFrameAdapter
 * 
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ApnsHttpHandlerFrameAdapter extends Http2FrameAdapter {

	private static final Logger log = LoggerFactory
			.getLogger(ApnsHttpHandlerFrameAdapter.class);

	@Override
	public void onSettingsRead(final ChannelHandlerContext context,
			final Http2Settings settings) {
		log.trace("Received settings from APNs gateway: {}", settings);
	}

	@Override
	public int onDataRead(final ChannelHandlerContext context,
			final int streamId, final ByteBuf data, final int padding,
			final boolean endOfStream) throws Http2Exception {
		log.trace("Received data from APNs gateway on stream {}: {}", streamId,
				data.toString(StandardCharsets.UTF_8));

		final int bytesProcessed = data.readableBytes() + padding;

		if (endOfStream) {
			final Http2Headers headers = ApnsHttpHandler.this.headersByStreamId
					.remove(streamId);
			final String authenticationToken = ApnsHttpHandler.this.authenticationTokensByStreamId
					.remove(streamId);
			final ApnsPushNotification pushNotification = ApnsHttpHandler.this.pushNotificationsByStreamId
					.remove(streamId);

			final HttpResponseStatus status = HttpResponseStatus
					.parseLine(headers.status());
			final String responseBody = data.toString(StandardCharsets.UTF_8);

			if (HttpResponseStatus.INTERNAL_SERVER_ERROR.equals(status)) {
				ApnsHttpHandler.this.apnsClient
						.handleServerError(pushNotification, responseBody);
			} else {
				final ErrorResponse errorResponse = gson.fromJson(responseBody,
						ErrorResponse.class);

				if (ApnsClient.EXPIRED_AUTH_TOKEN_REASON
						.equals(errorResponse.getReason())) {
					try {
						ApnsClientHandler.this.apnsClient
								.getAuthenticationTokenSupplierForTopic(
										pushNotification.getTopic())
								.invalidateToken(authenticationToken);
					} catch (final NoKeyForTopicException e) {
						// This should only happen if somebody de-registered
						// the topic after a notification was sent
						log.warn(
								"Authentication token expired, but no key registered for topic {}",
								pushNotification.getTopic());
					}
				}

				ApnsClientHandler.this.apnsClient
						.handlePushNotificationResponse(
								new SimplePushNotificationResponse<>(
										pushNotification,
										HttpResponseStatus.OK.equals(status),
										errorResponse.getReason(),
										errorResponse.getTimestamp()));
			}
		} else {
			log.error(
					"Gateway sent a DATA frame that was not the end of a stream.");
		}

		return bytesProcessed;
	}

	@Override
	public void onHeadersRead(final ChannelHandlerContext context,
			final int streamId, final Http2Headers headers,
			final int streamDependency, final short weight,
			final boolean exclusive, final int padding,
			final boolean endOfStream) throws Http2Exception {
		this.onHeadersRead(context, streamId, headers, padding, endOfStream);
	}

	@Override
	public void onHeadersRead(final ChannelHandlerContext context,
			final int streamId, final Http2Headers headers, final int padding,
			final boolean endOfStream) throws Http2Exception {
		log.trace("Received headers from APNs gateway on stream {}: {}",
				streamId, headers);

		if (endOfStream) {
			final HttpResponseStatus status = HttpResponseStatus
					.parseLine(headers.status());
			final boolean success = HttpResponseStatus.OK.equals(status);

			if (!success) {
				log.warn(
						"Gateway sent an end-of-stream HEADERS frame for an unsuccessful notification.");
			}

			final ApnsPushNotification pushNotification = ApnsClientHandler.this.pushNotificationsByStreamId
					.remove(streamId);
			ApnsClientHandler.this.authenticationTokensByStreamId
					.remove(streamId);

			if (HttpResponseStatus.INTERNAL_SERVER_ERROR.equals(status)) {
				ApnsClientHandler.this.apnsClient
						.handleServerError(pushNotification, null);
			} else {
				ApnsClientHandler.this.apnsClient
						.handlePushNotificationResponse(
								new SimplePushNotificationResponse<>(
										pushNotification, success, null, null));
			}
		} else {
			ApnsClientHandler.this.headersByStreamId.put(streamId, headers);
		}
	}

	@Override
	public void onPingAckRead(final ChannelHandlerContext context,
			final ByteBuf data) {
		if (ApnsClientHandler.this.pingTimeoutFuture != null) {
			log.trace("Received reply to ping.");
			ApnsClientHandler.this.pingTimeoutFuture.cancel(false);
		} else {
			log.error(
					"Received PING ACK, but no corresponding outbound PING found.");
		}
	}

	@Override
	public void onGoAwayRead(final ChannelHandlerContext context,
			final int lastStreamId, final long errorCode,
			final ByteBuf debugData) throws Http2Exception {
		log.info("Received GOAWAY from APNs server: {}",
				debugData.toString(StandardCharsets.UTF_8));
	}
}
