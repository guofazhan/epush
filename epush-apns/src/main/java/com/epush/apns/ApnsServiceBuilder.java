package com.epush.apns;

import com.epush.apns.authentication.P12;
import com.epush.apns.authentication.P8;
import com.epush.apns.http2.proxy.ProxyHandlerFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ApnsServiceBuilder {

	private static final Logger logger = LoggerFactory
			.getLogger(ApnsServiceBuilder.class);

	/**
	 * 环境信息
	 */
	private String environment;

	/**
	 * 代理信息
	 */
	private ProxyHandlerFactory proxyHandlerFactory;

	/**
	 * ssl信息
	 */
	private SslProvider sslProvider;

	/**
	 *
	 */
	private P12 p12;

	/**
	 *
	 */
	private P8 p8;

	private Long connectionTimeout;
	private TimeUnit connectionTimeoutUnit;

	private Long writeTimeout;
	private TimeUnit writeTimeoutUnit;

	private Long gracefulShutdownTimeout;
	private TimeUnit gracefulShutdownTimeoutUnit;

	protected ApnsServiceBuilder() {
	}

	/**
	 *
	 * @param p12File
	 * @param p12Password
	 * @throws SSLException
	 * @throws IOException
	 */
	public ApnsServiceBuilder setP12(final File p12File,
			final String p12Password) throws SSLException, IOException {
		p12 = P12.buildP12(p12File, p12Password);
		return this;
	}

	/**
	 *
	 * @param p12InputStream
	 * @param p12Password
	 * @throws SSLException
	 * @throws IOException
	 */
	public ApnsServiceBuilder setP12(final InputStream p12InputStream,
			final String p12Password) throws SSLException, IOException {
		p12 = P12.buildP12(p12InputStream, p12Password);
		return this;
	}

	public ApnsServiceBuilder setP8(final File signingKeyPemFile,
			final String teamId, final String keyId,
			final Collection<String> topics)
			throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		p8 = P8.buildP8(signingKeyPemFile, teamId, keyId, topics);
		return this;
	}

	/**
	 *
	 * @param signingKeyPemFile
	 * @param teamId
	 * @param keyId
	 * @param topics
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public ApnsServiceBuilder setP8(final File signingKeyPemFile,
			final String teamId, final String keyId, final String... topics)
			throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		p8 = P8.buildP8(signingKeyPemFile, teamId, keyId, topics);
		return this;
	}

	/**
	 *
	 * @param signingKeyInputStream
	 * @param teamId
	 * @param keyId
	 * @param topics
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public ApnsServiceBuilder setP8(final InputStream signingKeyInputStream,
			final String teamId, final String keyId,
			final Collection<String> topics)
			throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		p8 = P8.buildP8(signingKeyInputStream, teamId, keyId, topics);
		return this;
	}

	public ApnsServiceBuilder setP8(final InputStream signingKeyInputStream,
			final String teamId, final String keyId, final String... topics)
			throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		p8 = P8.buildP8(signingKeyInputStream, teamId, keyId, topics);
		return this;
	}

	public ApnsServiceBuilder setP8(final ECPrivateKey signingKey,
			final String teamId, final String keyId, final String... topics)
			throws InvalidKeyException, NoSuchAlgorithmException {
		p8 = P8.buildP8(signingKey, teamId, keyId, topics);
		return this;
	}

	public String getEnvironment() {
		return environment;
	}

	public ApnsServiceBuilder setEnvironment(String environment) {
		this.environment = environment;
		return this;
	}

	public ProxyHandlerFactory getProxyHandlerFactory() {
		return proxyHandlerFactory;
	}

	public ApnsServiceBuilder setProxyHandlerFactory(
			ProxyHandlerFactory proxyHandlerFactory) {
		this.proxyHandlerFactory = proxyHandlerFactory;
		return this;
	}

	public SslProvider getSslProvider() {
		return sslProvider;
	}

	public ApnsServiceBuilder setSslProvider(SslProvider sslProvider) {
		this.sslProvider = sslProvider;
		return this;
	}

	public ApnsServiceBuilder setConnectionTimeout(final long connectionTimeout,
			final TimeUnit timeoutUnit) {
		this.connectionTimeout = connectionTimeout;
		this.connectionTimeoutUnit = timeoutUnit;

		return this;
	}

	public ApnsServiceBuilder setWriteTimeout(final long writeTimeout,
			final TimeUnit timeoutUnit) {
		this.writeTimeout = writeTimeout;
		this.writeTimeoutUnit = timeoutUnit;
		return this;
	}

	public ApnsServiceBuilder setGracefulShutdownTimeout(
			final long gracefulShutdownTimeout, final TimeUnit timeoutUnit) {
		this.gracefulShutdownTimeout = gracefulShutdownTimeout;
		this.gracefulShutdownTimeoutUnit = timeoutUnit;
		return this;
	}

	/**
	 *
	 * @return
	 */
	public ApnsService build() {
		// 构建SslContext
		final SslContext sslContext = new SslContextBuilder()
				.setSslProvider(this.sslProvider).setP12(p12).build();
		final ApnsHttp2Client apnsClient = new ApnsHttp2Client();
		apnsClient.setEnvironment(this.environment);
		//
		apnsClient.setSslContext(sslContext);
		if (p8 != null) {
			apnsClient.setP8(p8);
			apnsClient.setUseTokenAuthentication(true);
		}

		// apnsClient.setMetricsListener(this.metricsListener);
		apnsClient.setProxyHandlerFactory(this.proxyHandlerFactory);

		if (this.connectionTimeout != null) {
			apnsClient.setConnectionTimeout((int) this.connectionTimeoutUnit
					.toMillis(this.connectionTimeout));
		}

		if (this.writeTimeout != null) {
			apnsClient.setWriteTimeout(
					this.writeTimeoutUnit.toMillis(this.writeTimeout));
		}

		if (this.gracefulShutdownTimeout != null) {
			apnsClient
					.setGracefulShutdownTimeout(this.gracefulShutdownTimeoutUnit
							.toMillis(this.gracefulShutdownTimeout));
		}

		apnsClient.init();
		return new ApnsService(apnsClient);
	}
}
