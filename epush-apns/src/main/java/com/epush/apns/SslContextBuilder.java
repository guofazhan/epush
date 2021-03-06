package com.epush.apns;

import com.epush.apns.authentication.P12;
import com.epush.apns.exception.InvalidSSLConfig;
import com.epush.apns.utils.Logger;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.InputStream;
import java.security.cert.X509Certificate;

/**
 * SslContext 构建器
 * 
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class SslContextBuilder {

	/**
	 *
	 */
	private P12 p12;

	/**
	 * 
	 */
	private SslProvider sslProvider;

	/**
	 * 
	 */
	private File trustedServerCertificatePemFile;

	/**
	 * 
	 */
	private InputStream trustedServerCertificateInputStream;

	/**
	 * 
	 */
	private X509Certificate[] trustedServerCertificates;

	public P12 getP12() {
		return p12;
	}

	public SslContextBuilder setP12(P12 p12) {
		this.p12 = p12;
		return this;
	}

	public SslProvider getSslProvider() {
		return sslProvider;
	}

	public SslContextBuilder setSslProvider(SslProvider sslProvider) {
		this.sslProvider = sslProvider;
		return this;
	}

	public File getTrustedServerCertificatePemFile() {
		return trustedServerCertificatePemFile;
	}

	public SslContextBuilder setTrustedServerCertificatePemFile(
			File trustedServerCertificatePemFile) {
		this.trustedServerCertificatePemFile = trustedServerCertificatePemFile;
		return this;
	}

	public InputStream getTrustedServerCertificateInputStream() {
		return trustedServerCertificateInputStream;
	}

	public SslContextBuilder setTrustedServerCertificateInputStream(
			InputStream trustedServerCertificateInputStream) {
		this.trustedServerCertificateInputStream = trustedServerCertificateInputStream;
		return this;
	}

	public X509Certificate[] getTrustedServerCertificates() {
		return trustedServerCertificates;
	}

	public SslContextBuilder setTrustedServerCertificates(
			X509Certificate[] trustedServerCertificates) {
		this.trustedServerCertificates = trustedServerCertificates;
		return this;
	}

	/**
	 * 构建 SslContext
	 * 
	 * @return
	 * @throws InvalidSSLConfig
	 */
	public SslContext build() throws InvalidSSLConfig {
		SslContext sslContext = null;
		{
			SslProvider sslProvider = null;
			if (this.sslProvider != null) {
				sslProvider = this.sslProvider;
			} else {
				if (OpenSsl.isAvailable()) {
					if (OpenSsl.isAlpnSupported()) {
						Logger.APNS.info(
								"Native SSL provider is available and supports ALPN; will use native provider.");
						sslProvider = SslProvider.OPENSSL;
					} else {
						Logger.APNS.info(
								"Native SSL provider is available, but does not support ALPN; will use JDK SSL provider.");
						sslProvider = SslProvider.JDK;
					}
				} else {
					Logger.APNS.info(
							"Native SSL provider not available; will use JDK SSL provider.");
					sslProvider = SslProvider.JDK;
				}
			}

			// 构建netty SslContextBuilder sslContextBuilder
			io.netty.handler.ssl.SslContextBuilder builder = io.netty.handler.ssl.SslContextBuilder
					.forClient().sslProvider(sslProvider)
					.ciphers(Http2SecurityUtil.CIPHERS,
							SupportedCipherSuiteFilter.INSTANCE)
					.applicationProtocolConfig(new ApplicationProtocolConfig(
							Protocol.ALPN, SelectorFailureBehavior.NO_ADVERTISE,
							SelectedListenerFailureBehavior.ACCEPT,
							ApplicationProtocolNames.HTTP_2));

			if (p12 != null) {
				builder.keyManager(p12.getPrivateKey(), p12.getKeyPassword(),
						p12.getClientCertificate());
			}

			if (this.trustedServerCertificatePemFile != null) {
				builder.trustManager(this.trustedServerCertificatePemFile);
			} else if (this.trustedServerCertificateInputStream != null) {
				builder.trustManager(this.trustedServerCertificateInputStream);
			} else if (this.trustedServerCertificates != null) {
				builder.trustManager(this.trustedServerCertificates);
			}
			try {
				// 开始构建SslContext
				sslContext = builder.build();
			} catch (SSLException e) {
				Logger.APNS.error("SslContext build Error:", e);
				throw new InvalidSSLConfig("SslContext build Error", e);
			}
		}

		return sslContext;
	}
}
