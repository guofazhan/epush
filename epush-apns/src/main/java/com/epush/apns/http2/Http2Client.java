package com.epush.apns.http2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http2 客户端
 * 
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public abstract class Http2Client {

	private static final Logger log = LoggerFactory
			.getLogger(Http2Client.class);
	/**
	 * bootstrap
	 */
	private Bootstrap bootstrap;

	/**
	 * 工作组
	 */
	private EventLoopGroup workerGroup;

	/**
	 *
	 */
	private boolean shouldShutDownEventLoopGroup;

	/**
	 * @param workerGroup
	 * @param channelFactory
	 */
	private void createClient(EventLoopGroup workerGroup,
			ChannelFactory<? extends Channel> channelFactory) {

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

	/**
	 * 开始连接
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public abstract ChannelFuture connect(String host, int port);

	public Bootstrap getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	public void setWorkerGroup(EventLoopGroup workerGroup) {
		this.workerGroup = workerGroup;
	}

	public boolean isShouldShutDownEventLoopGroup() {
		return shouldShutDownEventLoopGroup;
	}

	public void setShouldShutDownEventLoopGroup(
			boolean shouldShutDownEventLoopGroup) {
		this.shouldShutDownEventLoopGroup = shouldShutDownEventLoopGroup;
	}
}
