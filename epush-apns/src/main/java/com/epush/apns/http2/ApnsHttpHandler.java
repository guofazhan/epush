package com.epush.apns.http2;

import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.util.AsciiString;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.PromiseCombiner;
import io.netty.util.concurrent.ScheduledFuture;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ApnsHttpHandler extends Http2ConnectionHandler {

	private long nextStreamId = 1;

	private final Map<Integer, ApnsPushNotification> pushNotificationsByStreamId = new HashMap<>();
	private final Map<Integer, String> authenticationTokensByStreamId = new HashMap<>();
	private final Map<Integer, Http2Headers> headersByStreamId = new HashMap<>();

	private final ApnsHttp2Client http2Client;
	private final String authority;
	private final boolean useTokenAuthentication;

	private long nextPingId = new Random().nextLong();
	private ScheduledFuture<?> pingTimeoutFuture;

	private static final int PING_TIMEOUT = 30; // seconds

	private static final String APNS_PATH_PREFIX = "/3/device/";
	private static final AsciiString APNS_EXPIRATION_HEADER = new AsciiString(
			"apns-expiration");
	private static final AsciiString APNS_TOPIC_HEADER = new AsciiString(
			"apns-topic");
	private static final AsciiString APNS_PRIORITY_HEADER = new AsciiString(
			"apns-priority");
	private static final AsciiString APNS_COLLAPSE_ID_HEADER = new AsciiString(
			"apns-collapse-id");
	private static final AsciiString APNS_AUTHORIZATION_HEADER = new AsciiString(
			"authorization");

	private static final long STREAM_ID_RESET_THRESHOLD = Integer.MAX_VALUE - 1;

	private static final int INITIAL_PAYLOAD_BUFFER_CAPACITY = 4096;

	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(Date.class,
					new DateAsTimeSinceEpochTypeAdapter(TimeUnit.MILLISECONDS))
			.create();

	private static final Logger log = LoggerFactory
			.getLogger(ApnsHttpHandler.class);

	protected ApnsHttpHandler(final Http2ConnectionDecoder decoder,
			final Http2ConnectionEncoder encoder,
			final Http2Settings initialSettings,
			final ApnsHttp2Client http2Client, final String authority,
			final boolean useTokenAuthentication) {
		super(decoder, encoder, initialSettings);

		this.http2Client = http2Client;
		this.authority = authority;
		this.useTokenAuthentication = useTokenAuthentication;
	}


	private class ApnsClientHandlerFrameAdapter extends Http2FrameAdapter {

	}

	@Override
	public void write(final ChannelHandlerContext context, final Object message,
			final ChannelPromise writePromise) throws Http2Exception {
		try {
			try {
				// We'll catch class cast issues gracefully
				final ApnsPushNotification pushNotification = (ApnsPushNotification) message;

				final int streamId = (int) this.nextStreamId;

				final Http2Headers headers = new DefaultHttp2Headers()
						.method(HttpMethod.POST.asciiName())
						.authority(this.authority)
						.path(APNS_PATH_PREFIX + pushNotification.getToken())
						.addInt(APNS_EXPIRATION_HEADER,
								pushNotification.getExpiration() == null ? 0
										: (int) (pushNotification
												.getExpiration().getTime()
												/ 1000));

				final String authenticationToken;

				if (this.useTokenAuthentication) {
					authenticationToken = this.apnsClient
							.getAuthenticationTokenSupplierForTopic(
									pushNotification.getTopic())
							.getToken();
					headers.add(APNS_AUTHORIZATION_HEADER,
							"bearer " + authenticationToken);
				} else {
					authenticationToken = null;
				}

				if (pushNotification.getCollapseId() != null) {
					headers.add(APNS_COLLAPSE_ID_HEADER,
							pushNotification.getCollapseId());
				}

				if (pushNotification.getPriority() != null) {
					headers.addInt(APNS_PRIORITY_HEADER,
							pushNotification.getPriority().getCode());
				}

				if (pushNotification.getTopic() != null) {
					headers.add(APNS_TOPIC_HEADER, pushNotification.getTopic());
				}

				final ChannelPromise headersPromise = context.newPromise();
				this.encoder().writeHeaders(context, streamId, headers, 0,
						false, headersPromise);
				log.trace("Wrote headers on stream {}: {}", streamId, headers);

				final ByteBuf payloadBuffer = context.alloc()
						.ioBuffer(INITIAL_PAYLOAD_BUFFER_CAPACITY);
				payloadBuffer.writeBytes(pushNotification.getPayload()
						.getBytes(StandardCharsets.UTF_8));

				final ChannelPromise dataPromise = context.newPromise();
				this.encoder().writeData(context, streamId, payloadBuffer, 0,
						true, dataPromise);
				log.trace("Wrote payload on stream {}: {}", streamId,
						pushNotification.getPayload());

				final PromiseCombiner promiseCombiner = new PromiseCombiner();
				promiseCombiner.addAll(headersPromise, dataPromise);
				promiseCombiner.finish(writePromise);

				writePromise.addListener(
						new GenericFutureListener<ChannelPromise>() {

							@Override
							public void operationComplete(
									final ChannelPromise future)
									throws Exception {
								if (future.isSuccess()) {
									ApnsClientHandler.this.pushNotificationsByStreamId
											.put(streamId, pushNotification);

									if (ApnsClientHandler.this.useTokenAuthentication) {
										ApnsClientHandler.this.authenticationTokensByStreamId
												.put(streamId,
														authenticationToken);
									}
								} else {
									log.trace(
											"Failed to write push notification on stream {}.",
											streamId, future.cause());
								}
							}
						});

				this.nextStreamId += 2;

				if (this.nextStreamId >= STREAM_ID_RESET_THRESHOLD) {
					// This is very unlikely, but in the event that we run out
					// of stream IDs (the maximum allowed is
					// 2^31, per
					// https://httpwg.github.io/specs/rfc7540.html#StreamIdentifiers),
					// we need to open a new
					// connection. Just closing the context should be enough;
					// automatic reconnection should take things
					// from there.
					context.close();
				}
			} catch (NoKeyForTopicException | SignatureException e) {
				writePromise.tryFailure(e);
			}

		} catch (final ClassCastException e) {
			// This should never happen, but in case some foreign debris winds
			// up in the pipeline, just pass it through.
			log.error("Unexpected object in pipeline: {}", message);
			context.write(message, writePromise);
		}
	}

	@Override
	public void userEventTriggered(final ChannelHandlerContext context,
			final Object event) throws Exception {
		if (event instanceof IdleStateEvent) {
			assert PING_TIMEOUT < ApnsClient.PING_IDLE_TIME_MILLIS;

			log.trace("Sending ping due to inactivity.");

			final ByteBuf pingDataBuffer = context.alloc().ioBuffer(8, 8);
			pingDataBuffer.writeLong(this.nextPingId++);

			this.encoder()
					.writePing(context, false, pingDataBuffer,
							context.newPromise())
					.addListener(new GenericFutureListener<ChannelFuture>() {

						@Override
						public void operationComplete(
								final ChannelFuture future) throws Exception {
							if (future.isSuccess()) {
								ApnsClientHandler.this.pingTimeoutFuture = future
										.channel().eventLoop()
										.schedule(new Runnable() {

											@Override
											public void run() {
												log.debug(
														"Closing channel due to ping timeout.");
												future.channel().close();
											}
										}, PING_TIMEOUT, TimeUnit.SECONDS);
							} else {
								log.debug("Failed to write PING frame.",
										future.cause());
								future.channel().close();
							}
						}
					});

			this.flush(context);
		}

		super.userEventTriggered(context, event);
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext context,
			final Throwable cause) throws Exception {
		if (cause instanceof WriteTimeoutException) {
			log.debug("Closing connection due to write timeout.");
			context.close();
		} else {
			log.warn("APNs client pipeline caught an exception.", cause);
		}
	}
}
