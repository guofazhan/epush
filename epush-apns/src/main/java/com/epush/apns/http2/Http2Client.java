package com.epush.apns.http2;

import com.epush.apns.http2.exception.Http2Exception;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by G2Y on 2017/3/2.
 */
public class Http2Client implements Client {

    /**
     * 链接池
     */
    final Http2ConnectionPool pool = new Http2ConnectionPool(this);

    /**
     *
     */
    private static final long DEFAULT_READ_WRITER_OUTTIME = 5000;

    /**
     * 工作线程
     */
    private final EventLoopGroup workerGroup;

    /**
     * Http2ClientInitializer
     */
    private final Http2ClientInitializer initializer;

    /**
     *
     */
    private Bootstrap bootstrap;

    /**
     * 读写等待超时时间
     */
    private long readAndWriterTimeout;

    /**
     * @param workerGroup
     * @param initializer
     */
    protected Http2Client(EventLoopGroup workerGroup, Http2ClientInitializer initializer) {
        this(workerGroup, initializer, 0L);
    }


    /**
     * @param workerGroup
     * @param initializer
     * @param readAndWriterTimeout
     */
    public Http2Client(EventLoopGroup workerGroup, Http2ClientInitializer initializer, long readAndWriterTimeout) {
        if (workerGroup == null) {
            this.workerGroup = new NioEventLoopGroup();
        } else {
            this.workerGroup = workerGroup;
        }
        this.initializer = initializer;

        if (readAndWriterTimeout == 0) {
            this.readAndWriterTimeout = DEFAULT_READ_WRITER_OUTTIME;
        } else {
            this.readAndWriterTimeout = readAndWriterTimeout;
        }
    }


    /**
     * @throws Http2Exception
     */
    @Override
    public void init() throws Http2Exception {
        start();
    }

    /**
     * 启动http2客户端
     *
     * @return
     * @throws Http2Exception
     */
    @Override
    public boolean start() throws Http2Exception {

        if (this.workerGroup == null) {
            throw new Http2Exception("Netty EventLoopGroup is Empty!");
        }

        if (this.initializer == null) {
            throw new Http2Exception("Netty Http2ClientInitializer is Empty!");
        }

        this.bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(SocketChannelClassUtil.getSocketChannelClass(workerGroup));
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.handler(initializer);
        return true;
    }

    /**
     * @param request
     * @return
     * @throws Http2Exception
     */
    @Override
    public <T> Http2Response<T> request(Http2Request<T> request) throws Http2Exception {
        Http2Response<?> response = null;

        try {
            Channel channel = pool.tryAcquire(request.getHost());
            if (channel == null) {
                channel = pool.newChannel(request.getHost());
            }

            //等待http2设置
            Http2SettingsHandler settingsHandler = initializer.getSettingsHandler();
            settingsHandler.awaitSettings(9000, TimeUnit.MILLISECONDS);

            if (null == channel) {
                response = Http2Response.toHttp2Response(new Http2Exception("GET CONNECTION Fail!"));
            } else {
                HttpResponseHandler responseHandler = initializer.getResponseHandler();
                int streamId = channel.attr(pool.streamIdKey).get();
                responseHandler.put(streamId, channel.writeAndFlush(request), channel.newPromise());
                FullHttpResponse httpResponse = responseHandler.getResponse(streamId, 60000, TimeUnit.MILLISECONDS);
                if (httpResponse != null) {
                    response = Http2Response.toHttp2Response(httpResponse);
                }
                System.out.println("Finished HTTP/2 request(s)");
            }
        } catch (Http2Exception e) {
            response = Http2Response.toHttp2Response(e);
        } catch (Exception e) {
            response = Http2Response.toHttp2Response(new Http2Exception(e));
        }
        return (Http2Response<T>) response;
    }


    /**
     * 检验http2客户端是否运行
     *
     * @return
     * @throws Http2Exception
     */
    @Override
    public boolean isRuning() throws Http2Exception {
        return null != this.bootstrap && null != this.workerGroup && !this.workerGroup.isShutdown();
    }

    /**
     * 关闭当前http2客户端
     *
     * @return
     * @throws Http2Exception
     */
    @Override
    public boolean shutdown() throws Http2Exception {
        if (null != this.bootstrap) {
            synchronized (this.bootstrap) {
                if (null != this.workerGroup && !this.workerGroup.isShutdown()) {
                    Future<?> future = this.workerGroup.shutdownGracefully();
                    try {
                        future.get(30, TimeUnit.SECONDS);

                        if (future.isSuccess()) {
                            return true;
                        }
                    } catch (InterruptedException e) {
                        throw new Http2Exception(e);
                    } catch (ExecutionException e) {
                        throw new Http2Exception(e);
                    } catch (TimeoutException e) {
                        throw new Http2Exception(e);
                    }

                }
            }
        }

        return false;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }
}
