package com.epush.apns.http2;

import com.google.common.collect.ArrayListMultimap;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Created by G2Y on 2017/2/28.
 */
public class Http2ConnectionPool {

    private static final int maxConnPerHost = 20;

    private static final Logger logger = LoggerFactory.getLogger(Http2ConnectionPool.class);

    private final AttributeKey<String> hostKey = AttributeKey.newInstance("host");

    private final ArrayListMultimap<String, Channel> channelPool = ArrayListMultimap.create();


    public synchronized Channel tryAcquire(String host) {
        List<Channel> channels = channelPool.get(host);
        if (channels == null || channels.isEmpty()) return null;
        Iterator<Channel> it = channels.iterator();
        while (it.hasNext()) {
            Channel channel = it.next();
            it.remove();
            if (channel.isActive()) {
                logger.debug("tryAcquire channel success, host={}", host);
                channel.attr(hostKey).set(host);
                return channel;
            } else {//链接由于意外情况不可用了, 比如: keepAlive_timeout
                logger.warn("tryAcquire channel false channel is inactive, host={}", host);
            }
        }
        return null;
    }

    public synchronized void tryRelease(Channel channel) {
        String host = channel.attr(hostKey).getAndSet(null);
        List<Channel> channels = channelPool.get(host);
        if (channels == null || channels.size() < maxConnPerHost) {
            logger.debug("tryRelease channel success, host={}", host);
            channelPool.put(host, channel);
        } else {
            logger.debug("tryRelease channel pool size over limit={}, host={}, channel closed.", maxConnPerHost, host);
            channel.close();
        }
    }

    public void attachHost(String host, Channel channel) {
        channel.attr(hostKey).set(host);
    }

    public void close() {
        for (Channel channel : channelPool.values()) {
            channel.close();
        }

        channelPool.clear();
    }
}
