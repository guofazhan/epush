package com.epush.apns.http2;

import com.epush.apns.utils.Logger;
import com.google.common.collect.ArrayListMultimap;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by G2Y on 2017/2/28.
 */
public class Http2ConnectionPool {

	/**
	 *
	 */
	private static final int maxConnPerHost = 20;

	/**
	 *
	 */
	private static final int DEFAULT_STREAM_ID = 3;

	/**
	 *
	 */
	private final AttributeKey<Host> hostKey = AttributeKey.newInstance("host");

	/**
	 *
	 */
	public static final AttributeKey<Integer> streamIdKey = AttributeKey
			.newInstance("streamId");

	/**
	 *
	 */
	private final ArrayListMultimap<String, Channel> channelPool = ArrayListMultimap
			.create();

	/**
	 *
	 */
	private static final long DEFAULT_CONNECT_OUTTIME = 5000;

	/**
	 *
	 */
	private static final long STREAM_ID_RESET_THRESHOLD = Integer.MAX_VALUE - 1;

	/**
	 *
	 */
	private final Http2Client client;

	/**
	 * @param client
	 */
	Http2ConnectionPool(Http2Client client) {
		this.client = client;
	}

	/**
	 * @param host
	 * @return
	 */
	public synchronized Channel tryAcquire(Host host) {
		List<Channel> channels = channelPool.get(host.toString());
		if (channels == null || channels.isEmpty())
			return null;
		Iterator<Channel> it = channels.iterator();
		while (it.hasNext()) {
			Channel channel = it.next();
			it.remove();
			if (channel.isActive()) {
				com.epush.apns.utils.Logger.HTTP2
						.debug("tryAcquire channel success, host={}", host);
				channel.attr(hostKey).set(host);
				return channel;
			} else {
				// 链接由于意外情况不可用了, 比如: keepAlive_timeout
				Logger.HTTP2.warn(
						"tryAcquire channel false channel is inactive, host={}",
						host);
			}
		}
		return null;
	}

	/**
	 * @param host
	 * @return
	 */
	public synchronized Channel newChannel(Host host) throws Exception {
		Channel channel = null;
		if (this.client.isRuning()) {
			channel = this.client.getBootstrap()
					.connect(host.getHost(), host.getPort())
					.syncUninterruptibly().channel();
			Logger.HTTP2.info("Connected to Ip {},port {}", host.getHost(),
					host.getPort());
			if (channel != null && channel.isActive()) {
				// 等待http2设置
				Http2SettingsHandler settingsHandler = this.client
						.getInitializer().getSettingsHandler();
				settingsHandler.awaitSettings(3000, TimeUnit.MILLISECONDS);
				// 设置当前channel属性信息
				attachHost(host, channel);
				attachStreamId(DEFAULT_STREAM_ID, channel);
			}
		} else {
			Logger.HTTP2.warn("get connection Error Client is shutdown");
		}
		return channel;
	}

	/**
	 * @param channel
	 */
	public synchronized void tryRelease(Channel channel) {
		Host host = channel.attr(hostKey).getAndSet(null);
		List<Channel> channels = channelPool.get(host.toString());
		if (channels == null || channels.size() < maxConnPerHost) {
			Integer streamId = channel.attr(streamIdKey).get();
			streamId += 2;
			if (streamId >= STREAM_ID_RESET_THRESHOLD) {
				Logger.HTTP2.debug(
						"tryRelease channel pool size over streamId={}, host={}, channel closed.",
						streamId, host);
				channel.close();
			} else {
				if (channel.isActive()) {
					Logger.HTTP2.debug("tryRelease channel success, host={}", host);
					channel.attr(streamIdKey).set(streamId);
					channelPool.put(host.toString(), channel);
				}
			}
		} else {
			Logger.HTTP2.debug(
					"tryRelease channel pool size over limit={}, host={}, channel closed.",
					maxConnPerHost, host);
			channel.close();
		}
	}

	/**
	 * @param host
	 * @param channel
	 */
	public void attachHost(Host host, Channel channel) {
		channel.attr(hostKey).set(host);
	}

	/**
	 * @param streamId
	 * @param channel
	 */
	public void attachStreamId(Integer streamId, Channel channel) {
		channel.attr(streamIdKey).set(streamId);
	}

	/**
	 *
	 */
	public void close() {
		for (Channel channel : channelPool.values()) {
			channel.close();
		}
		channelPool.clear();
	}
}
