package com.epush.apns.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.*;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;


/**
 * Created by G2Y on 2017/3/4.
 */
public class DefaultHttp2ConnectionHandlerBuilder extends AbstractHttp2ConnectionHandlerBuilder<DefaultHttp2ConnectionHandler, DefaultHttp2ConnectionHandlerBuilder> {

    private final int maxContentLength;


    private static final Logger log = LoggerFactory.getLogger(DefaultHttp2FrameAdapter.class);

    public DefaultHttp2ConnectionHandlerBuilder(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    @Override
    public DefaultHttp2ConnectionHandlerBuilder server(boolean isServer) {
        return super.server(isServer);
    }

    @Override
    public DefaultHttp2ConnectionHandlerBuilder connection(Http2Connection connection) {
        return super.connection(connection);
    }

    @Override
    public DefaultHttp2ConnectionHandlerBuilder frameLogger(Http2FrameLogger frameLogger) {
        return super.frameLogger(frameLogger);
    }

    @Override
    protected DefaultHttp2ConnectionHandlerBuilder encoderEnforceMaxConcurrentStreams(boolean encoderEnforceMaxConcurrentStreams) {
        return super.encoderEnforceMaxConcurrentStreams(encoderEnforceMaxConcurrentStreams);
    }

    /**
     * DefaultHttp2Connection 构建器
     *
     * @param decoder
     * @param encoder
     * @param http2Settings
     * @return
     * @throws Exception
     */
    @Override
    protected DefaultHttp2ConnectionHandler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder, Http2Settings http2Settings) throws Exception {
        final DefaultHttp2ConnectionHandler handler = new DefaultHttp2ConnectionHandler(decoder, encoder, http2Settings, getMaxContentLength());
        this.frameListener(new DefaultHttp2FrameAdapter());
        return handler;
    }

    @Override
    public DefaultHttp2ConnectionHandler build() {
        return super.build();
    }

    /**
     *
     */
    class DefaultHttp2FrameAdapter extends Http2FrameAdapter {

        private final Map<Integer, Http2Headers> streamidHeaderMap;

        public DefaultHttp2FrameAdapter() {
            streamidHeaderMap = PlatformDependent.newConcurrentHashMap();
        }

        @Override
        public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
            log.trace("Received data from APNs gateway on stream {}: {}", streamId, data.toString(StandardCharsets.UTF_8));
            System.out.println("Received data from APNs gateway on stream {}: {}" + data.toString(StandardCharsets.UTF_8));
            final int bytesProcessed = data.readableBytes() + padding;
            if (endOfStream) {
                final String responseBody = data.toString(StandardCharsets.UTF_8);
                Http2Headers headers = streamidHeaderMap.remove(streamId);
                Http2Response<String> response = null;
                if (headers != null) {
                    response = new Http2Response<String>(headers);
                    response.setRequest(responseBody);
                } else {
                    response = new Http2Response<String>();
                    response.setRequest("Header is not fond");
                }
                ctx.fireChannelRead(response);
            } else {
                log.error("Gateway sent a DATA frame that was not the end of a stream.");
            }

            return bytesProcessed;
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endOfStream) throws Http2Exception {
            log.trace("Received headers from APNs gateway on stream {}: {}", streamId, headers);
            if (endOfStream) {
                System.out.println("Received headers from APNs gateway on stream {}: {}" + headers);
                streamidHeaderMap.put(streamId, headers);
            } else {
                log.trace("Gateway sent a Header frame that was not the end of a stream.");
            }
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext context, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endOfStream) throws Http2Exception {
            this.onHeadersRead(context, streamId, headers, padding, endOfStream);
        }

        @Override
        public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) throws Http2Exception {
            System.out.println("Http2Settings : " + settings);
            log.trace("Received settings from APNs gateway: {}", settings);
            ctx.fireChannelRead(settings);
        }


        @Override
        public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) throws Http2Exception {
            log.info("Received GOAWAY from APNs server: {}", debugData.toString(StandardCharsets.UTF_8));
        }
    }
}
