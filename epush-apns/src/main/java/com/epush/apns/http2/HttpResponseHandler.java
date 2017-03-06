package com.epush.apns.http2;

import com.epush.apns.http2.exception.Http2Exception;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.PlatformDependent;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by G2Y on 2017/3/4.
 */
public class HttpResponseHandler
		extends SimpleChannelInboundHandler<FullHttpResponse> {
	/**
	 * Future集合
	 */
	private final Map<Integer, Map.Entry<ChannelFuture, ChannelPromise>> streamidPromiseMap;

	/**
	 * 响应结果集
	 */
	private final Map<Integer, FullHttpResponse> streamidRespMap;

	public HttpResponseHandler() {
		streamidPromiseMap = PlatformDependent.newConcurrentHashMap();
		streamidRespMap = PlatformDependent.newConcurrentHashMap();
	}

	public Map.Entry<ChannelFuture, ChannelPromise> put(int streamId,
			ChannelFuture writeFuture, ChannelPromise promise) {
		return streamidPromiseMap.put(streamId,
				new AbstractMap.SimpleEntry<ChannelFuture, ChannelPromise>(
						writeFuture, promise));
	}

	/**
	 * @param streamId
	 * @param timeout
	 * @param unit
	 * @return
	 */
	public FullHttpResponse getResponse(int streamId, long timeout,
			TimeUnit unit) {
		FullHttpResponse response = null;
		Map.Entry<ChannelFuture, ChannelPromise> entry = streamidPromiseMap
				.get(streamId);
		if (null != entry) {
			ChannelFuture writeFuture = entry.getKey();
			if (!writeFuture.awaitUninterruptibly(timeout, unit)) {
				throw new Http2Exception(new IllegalStateException(
						"Timed out waiting to write for stream id "
								+ streamId));
			} else if (!writeFuture.isSuccess()) {
				throw new Http2Exception(writeFuture.cause());
			} else {
				ChannelPromise responsePromise = entry.getValue();
				if (!responsePromise.awaitUninterruptibly(timeout, unit)) {
					throw new Http2Exception(new IllegalStateException(
							"Timed out waiting for response on stream id "
									+ streamId));
				} else if (!responsePromise.isSuccess()) {
					throw new Http2Exception(responsePromise.cause());
				} else {
					// 获取response响应
					response = streamidRespMap.remove(streamId);
				}
			}

			// 移除当前的请求缓存
			streamidPromiseMap.remove(streamId);
		}

		return response;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg)
			throws Exception {
		Integer streamId = msg.headers().getInt(
				HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
		if (streamId == null) {
			System.err.println(
					"HttpResponseHandler unexpected message received: " + msg);
			return;
		}

		Map.Entry<ChannelFuture, ChannelPromise> entry = streamidPromiseMap
				.get(streamId);
		if (entry == null) {
			System.err.println(
					"Message received for unknown stream id " + streamId);
		} else {
			// Do stuff with the message (for now just print it)
			ByteBuf content = msg.content();
			if (content.isReadable()) {
				int contentLength = content.readableBytes();
				byte[] arr = new byte[contentLength];
				content.readBytes(arr);
				System.out.println(
						new String(arr, 0, contentLength, CharsetUtil.UTF_8));
			}
			streamidRespMap.put(streamId, msg);
			entry.getValue().setSuccess();
		}
	}
}
