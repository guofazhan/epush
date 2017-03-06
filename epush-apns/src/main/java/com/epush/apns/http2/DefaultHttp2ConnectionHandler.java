package com.epush.apns.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.PromiseCombiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Created by G2Y on 2017/3/4.
 */
public class DefaultHttp2ConnectionHandler extends Http2ConnectionHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultHttp2ConnectionHandler.class);

    private final int maxContentLength;


    protected DefaultHttp2ConnectionHandler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder, Http2Settings initialSettings, int maxContentLength) {
        super(decoder, encoder, initialSettings);
        this.maxContentLength = maxContentLength;
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext context, final Object event) throws Exception {
        super.userEventTriggered(context, event);
    }


    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) throws Exception {
        cause.printStackTrace();
        log.warn("APNs client pipeline caught an exception.", cause);
    }


    @Override
    public void write(ChannelHandlerContext context, Object message, ChannelPromise writePromise) throws Exception {
        try {
            try {
                // 获取当前 streamId
                final int streamId = context.channel().attr(Http2ConnectionPool.streamIdKey).get();

                System.out.println("Start write Msg By streamId " + streamId);
                // We'll catch class cast issues gracefully
                final Http2Request request = (Http2Request) message;

                //写入Headers
                final ChannelPromise headersPromise = context.newPromise();
                this.encoder().writeHeaders(context, streamId, request.getHeaders(), 0, false, headersPromise);
                log.trace("Wrote headers on stream {}: {}", streamId, request.getHeaders());

                System.out.println("Wrote headers on stream {}: {}" + request.getHeaders());

                //写入body
                final ChannelPromise dataPromise = context.newPromise();
                final ByteBuf payloadBuffer = context.alloc().ioBuffer(maxContentLength);
                payloadBuffer.writeBytes(request.getRequest().toString().getBytes(StandardCharsets.UTF_8));
                this.encoder().writeData(context, streamId, payloadBuffer, 0, true, dataPromise);
                log.trace("Wrote payload on stream {}: {}", streamId, request.getRequest().toString());
                System.out.println("Wrote payload on stream {}: {}" + request.getRequest().toString().getBytes(StandardCharsets.UTF_8));
                final PromiseCombiner promiseCombiner = new PromiseCombiner();
                promiseCombiner.addAll(headersPromise, dataPromise);

                promiseCombiner.finish(writePromise);
                writePromise.addListener(new GenericFutureListener<ChannelPromise>() {
                    @Override
                    public void operationComplete(final ChannelPromise future) throws Exception {
                        log.trace("Failed to write push notification on stream {}.", streamId, future.cause());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                writePromise.tryFailure(e);
            }
        } catch (final ClassCastException e) {
            e.printStackTrace();
            log.error("Unexpected object in pipeline: {}", message);
            context.write(message, writePromise);
        }
    }
}
