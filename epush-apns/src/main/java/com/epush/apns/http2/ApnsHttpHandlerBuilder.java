package com.epush.apns.http2;

import io.netty.handler.codec.http2.AbstractHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Settings;

import java.util.Objects;

/**
 * HttpHandlerBuilder
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ApnsHttpHandlerBuilder extends
		AbstractHttp2ConnectionHandlerBuilder<ApnsHttpHandler, ApnsHttpHandlerBuilder> {

	private ApnsHttp2Client http2Client;
	private String authority;
	private boolean useTokenAuthentication;

	public ApnsHttpHandlerBuilder setHttp2Client(
			final ApnsHttp2Client http2Client) {
		this.http2Client = http2Client;
		return this;
	}

	public ApnsHttp2Client getHttp2Client() {
		return this.http2Client;
	}

	public ApnsHttpHandlerBuilder setAuthority(final String authority) {
		this.authority = authority;
		return this;
	}

	public String getAuthority() {
		return this.authority;
	}

	public ApnsHttpHandlerBuilder useTokenAuthentication(
			final boolean useTokenAuthentication) {
		this.useTokenAuthentication = useTokenAuthentication;
		return this;
	}

	public boolean useTokenAuthentication() {
		return this.useTokenAuthentication;
	}

	@Override
	public ApnsHttpHandlerBuilder server(final boolean isServer) {
		return super.server(isServer);
	}

	@Override
	public ApnsHttpHandlerBuilder encoderEnforceMaxConcurrentStreams(
			final boolean enforceMaxConcurrentStreams) {
		return super.encoderEnforceMaxConcurrentStreams(
				enforceMaxConcurrentStreams);
	}

	@Override
	public ApnsHttpHandler build(final Http2ConnectionDecoder decoder,
			final Http2ConnectionEncoder encoder,
			final Http2Settings initialSettings) {
		Objects.requireNonNull(getAuthority(),
				"Authority must be set before building an ApnsClientHandler.");
		final ApnsHttpHandler handler = new ApnsHttpHandler(decoder, encoder,
				initialSettings, getHttp2Client(), getAuthority(),
				this.useTokenAuthentication());
		this.frameListener(new ApnsHttpHandlerFrameAdapter());
		return handler;
	}

	@Override
	public ApnsHttpHandler build() {
		return super.build();
	}
}
