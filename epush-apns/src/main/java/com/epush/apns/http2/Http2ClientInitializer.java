package com.epush.apns.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;

import static io.netty.handler.logging.LogLevel.INFO;

/**
 * Created by G2Y on 2017/3/2.
 */
public abstract class Http2ClientInitializer extends ChannelInitializer<SocketChannel> {

    private static final Http2FrameLogger logger = new Http2FrameLogger(INFO, Http2ClientInitializer.class);
    private final SslContext sslCtx;
    private final int maxContentLength;
    private HttpToHttp2ConnectionHandler connectionHandler;
    private DefaultHttp2ConnectionHandler connectionHandler1;
    private Http2SettingsHandler settingsHandler;

    private HttpResponseHandler responseHandler;

    public Http2ClientInitializer(SslContext sslCtx, int maxContentLength) {
        this.sslCtx = sslCtx;
        this.maxContentLength = maxContentLength;
    }

    public HttpResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public Http2SettingsHandler getSettingsHandler() {
        return settingsHandler;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        responseHandler = new HttpResponseHandler();
        settingsHandler = new Http2SettingsHandler(socketChannel.newPromise());
        final Http2Connection connection = new DefaultHttp2Connection(false);
        connectionHandler1 =
                new DefaultHttp2ConnectionHandlerBuilder(maxContentLength).server(false).encoderEnforceMaxConcurrentStreams(true).frameLogger(logger).build();

//        connectionHandler = new HttpToHttp2ConnectionHandlerBuilder()
//                .frameListener(new DelegatingDecompressorFrameListener(
//                        connection,
//                        new InboundHttp2ToHttpAdapterBuilder(connection)
//                                .maxContentLength(maxContentLength)
//                                .propagateSettings(true)
//                                .build()))
//                .frameLogger(logger)
//                .connection(connection)
//                .build();
        configure(socketChannel);
        if (sslCtx != null) {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
            // We must wait for the handshake to finish and the protocol to be negotiated before configuring
            // the HTTP/2 components of the pipeline.
            pipeline.addLast(new ApplicationProtocolNegotiationHandler("") {
                @Override
                protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                    if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                        ChannelPipeline p = ctx.pipeline();
                        //p.addLast(connectionHandler);
                        p.addLast(connectionHandler1);
                        configureEndOfPipeline(p);
                        return;
                    }
                    ctx.close();
                    throw new IllegalStateException("unknown protocol: " + protocol);
                }
            });

        } else {
            throw new IllegalStateException("unknown SslContext: " + sslCtx);
        }


    }

    protected void configureEndOfPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(settingsHandler, responseHandler);
    }

    public abstract void configure(SocketChannel channel);

}
