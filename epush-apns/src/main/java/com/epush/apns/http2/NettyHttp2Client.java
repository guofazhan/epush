package com.epush.apns.http2;

import com.epush.apns.http2.exception.Http2Exception;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.AttributeKey;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Created by G2Y on 2017/2/28.
 */
public abstract class NettyHttp2Client implements Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttp2Client.class);

    final AttributeKey<RequestContext> requestKey = AttributeKey.newInstance("request");

    final Http2ConnectionPool pool = new Http2ConnectionPool();

    private Timer timer;

    /**
     * bootstrap
     */
    private Bootstrap bootstrap;

    /**
     * 工作组
     */
    private EventLoopGroup workerGroup;

    private ChannelFactory<? extends Channel> channelFactory;

    /**
     *
     */
    private boolean shouldShutDownEventLoopGroup;

    /**
     * @param context
     * @throws Exception
     */
    public void request(final RequestContext context) throws Http2Exception {
        URI uri = new URI(context.request.uri());
        String host = context.host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();
        //2.添加请求超时检测队列
        timer.newTimeout(context, context.readTimeout, TimeUnit.MILLISECONDS);

        //3.先尝试从连接池里取可用链接，去取不到就创建新链接。
        Channel channel = pool.tryAcquire(host);
        if (channel == null) {
            final long startCreate = System.currentTimeMillis();
            LOGGER.debug("create new channel, host={}", host);
            ChannelFuture f = bootstrap.connect(host, port);
            f.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    LOGGER.debug("create new channel cost={}", (System.currentTimeMillis() - startCreate));
                    if (channelFuture.isSuccess()) {
                        //3.1.把请求写到http server
                        writeRequest(channelFuture.channel(), context);
                    } else {
                        //3.2如果链接创建失败，直接返回客户端网关超时
                        context.tryDone();
                        context.onFailure(504, "Gateway Timeout");
                        LOGGER.warn("create new channel failure, request={}", context);
                    }
                }
            });
        } else {
            //3.1.把请求写到http server
            writeRequest(channel, context);
        }
    }

    /**
     * 写入请求
     *
     * @param channel
     * @param context
     */
    private void writeRequest(Channel channel, RequestContext context) {
        channel.attr(requestKey).set(context);
        pool.attachHost(context.host, channel);

        channel.writeAndFlush(context.request)
                .addListener(new ChannelFutureListener() {
                                 public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                     if (!channelFuture.isSuccess()) {
                                         RequestContext info = channelFuture.channel().attr(requestKey).getAndSet(null);
                                         info.tryDone();
                                         info.onFailure(503, "Service Unavailable");
                                         LOGGER.debug("request failure request={}", info);
                                         pool.tryRelease(channelFuture.channel());
                                     }
                                 }
                             }
                );
    }

    public void init() throws Http2Exception {
        this.bootstrap = new Bootstrap();

        if (workerGroup != null) {
            this.bootstrap.group(workerGroup);
            this.shouldShutDownEventLoopGroup = false;
        } else {
            this.bootstrap.group(new NioEventLoopGroup(1));
            this.shouldShutDownEventLoopGroup = true;
        }
        this.bootstrap.option(ChannelOption.TCP_NODELAY, true);
        this.bootstrap.channelFactory(channelFactory);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            public void initChannel(Channel ch) throws Exception {
                initPipeline(ch);
            }
        });
        initOptions(bootstrap);
        timer = new HashedWheelTimer(1, TimeUnit.SECONDS, 64);
    }

    /**
     * 初始化管道信息
     *
     * @param ch
     */
    protected abstract void initPipeline(Channel ch);

    /**
     * 配置初始化
     *
     * @param b
     */
    protected abstract void initOptions(Bootstrap b);
}
