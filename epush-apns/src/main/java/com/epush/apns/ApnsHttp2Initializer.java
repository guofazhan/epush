package com.epush.apns;

import com.epush.apns.http2.Http2ClientInitializer;
import com.epush.apns.http2.proxy.ProxyHandlerFactory;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;

/**
 * Created by G2Y on 2017/3/3.
 */
public class ApnsHttp2Initializer extends Http2ClientInitializer {


    /**
     * 代理信息
     */
    private final ProxyHandlerFactory proxyHandlerFactory;


    public ApnsHttp2Initializer(SslContext sslCtx, ProxyHandlerFactory proxyHandlerFactory, int maxContentLength) {
        super(sslCtx, maxContentLength);
        this.proxyHandlerFactory = proxyHandlerFactory;
    }

    @Override
    public void configure(SocketChannel channel) {
        final ChannelPipeline pipeline = channel.pipeline();
        if (proxyHandlerFactory != null) {
            pipeline.addFirst(proxyHandlerFactory.buildProxyHandler());
        }
    }
}
