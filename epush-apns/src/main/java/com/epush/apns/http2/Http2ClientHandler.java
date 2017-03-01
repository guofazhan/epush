package com.epush.apns.http2;

import com.epush.apns.ApnsConfigure;
import com.epush.apns.ApnsPushNotification;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.PromiseCombiner;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by G2Y on 2017/2/28.
 */
public class Http2ClientHandler extends Http2ConnectionHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(Http2ClientHandler.class);

	private final NettyHttp2Client client;
	private final String authority;
	private final boolean useTokenAuthentication;

	private long nextPingId = new Random().nextLong();
	private ScheduledFuture<?> pingTimeoutFuture;

	private static final long STREAM_ID_RESET_THRESHOLD = Integer.MAX_VALUE - 1;

	private static final int INITIAL_PAYLOAD_BUFFER_CAPACITY = 4096;

	private static final int PING_TIMEOUT = 30; // seconds

	private long nextStreamId = 1;

	protected Http2ClientHandler(Http2ConnectionDecoder decoder,
			Http2ConnectionEncoder encoder, Http2Settings initialSettings,
			NettyHttp2Client client, String authority,
			boolean useTokenAuthentication) {
		super(decoder, encoder, initialSettings);
		this.client = client;
		this.authority = authority;
		this.useTokenAuthentication = useTokenAuthentication;
	}

	@Override
	public void write(final ChannelHandlerContext context, final Object message,
			final ChannelPromise writePromise) throws Http2Exception {
		try {
			// We'll catch class cast issues gracefully
			Http2Request reqest = (Http2Request) message;
			final ApnsPushNotification pushNotification = (ApnsPushNotification) reqest
					.getData();
			Http2Request.Http2HeadersBuilder builder = Http2Request.newBuilder()
					.setAuthority(this.authority);

			builder.setPath(ApnsConfigure.APNS_PATH_PREFIX
					+ pushNotification.getToken());
			// builder.addParam(ApnsConfigure.APNS_EXPIRATION_HEADER,pushNotification.g)

			if (pushNotification.getCollapseId() != null) {
				builder.addParam(ApnsConfigure.APNS_COLLAPSE_ID_HEADER,
						pushNotification.getCollapseId());
			}

			if (pushNotification.getPriority() != null) {
				builder.addParam(ApnsConfigure.APNS_PRIORITY_HEADER,
						pushNotification.getPriority().getCode());
			}

			if (pushNotification.getTopic() != null) {
				builder.addParam(ApnsConfigure.APNS_TOPIC_HEADER,
						pushNotification.getTopic());
			}

			reqest.setHeader(builder.build());
			final int streamId = (int) this.nextStreamId;
			logger.trace("Wrote headers on stream {}: {}", streamId,
					reqest.getHeader());
			final ChannelPromise headersPromise = context.newPromise();
			this.encoder().writeHeaders(context, streamId, reqest.getHeader(),
					0, false, headersPromise);

			final ByteBuf payloadBuffer = context.alloc()
					.ioBuffer(INITIAL_PAYLOAD_BUFFER_CAPACITY);
			payloadBuffer.writeBytes(pushNotification.getPayload()
					.getBytes(StandardCharsets.UTF_8));

			final ChannelPromise dataPromise = context.newPromise();
			this.encoder().writeData(context, streamId, payloadBuffer, 0, true,
					dataPromise);
			logger.trace("Wrote payload on stream {}: {}", streamId,
					pushNotification.getPayload());
			final PromiseCombiner promiseCombiner = new PromiseCombiner();
			promiseCombiner.addAll(headersPromise, dataPromise);
			promiseCombiner.finish(writePromise);
			this.nextStreamId += 2;

			if (this.nextStreamId >= STREAM_ID_RESET_THRESHOLD) {
				context.close();
			}

		} catch (final ClassCastException e) {
			logger.error("Unexpected object in pipeline: {}", message);
			context.write(message, writePromise);
		}
	}

	@Override
	public void userEventTriggered(final ChannelHandlerContext context,
			final Object event) throws Exception {
		if (event instanceof IdleStateEvent) {
			logger.trace("Sending ping due to inactivity.");
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
								Http2ClientHandler.this.pingTimeoutFuture = future
										.channel().eventLoop()
										.schedule(new Runnable() {
											@Override
											public void run() {
												logger.debug(
														"Closing channel due to ping timeout.");
												future.channel().close();
											}
										}, PING_TIMEOUT, TimeUnit.SECONDS);
							} else {
								logger.debug("Failed to write PING frame.",
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
	public void exceptionCaught(ChannelHandlerContext context, Throwable cause)
			throws Exception {
		RequestContext rcontext = context.channel().attr(client.requestKey)
				.getAndRemove();
		try {
			if (context != null && rcontext.tryDone()) {
				rcontext.onException(cause);
			}
		} finally {
			client.pool.tryRelease(context.channel());
		}
		logger.error("http client caught an ex, info={}", context, cause);
	}
}
