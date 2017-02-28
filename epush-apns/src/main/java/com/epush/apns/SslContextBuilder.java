package com.epush.apns;

import com.epush.apns.exception.InvalidSSLConfig;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
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

	private static final Logger logger = LoggerFactory
			.getLogger(SslContextBuilder.class);

	/**
	 * 客户端证书
	 */
	private X509Certificate clientCertificate;

	/**
	 * 客户端私钥
	 */
	private PrivateKey privateKey;

	/**
	 * 私钥密码
	 */
	private String privateKeyPassword;

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


    /**
     * @return
     */
    public X509Certificate getClientCertificate() {
        return clientCertificate;
    }

    /**
     * @param clientCertificate
     */
    public SslContextBuilder setClientCertificate(X509Certificate clientCertificate) {
        this.clientCertificate = clientCertificate;
        return this;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public SslContextBuilder setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    public SslContextBuilder setPrivateKeyPassword(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
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

    public SslContextBuilder setTrustedServerCertificatePemFile(File trustedServerCertificatePemFile) {
        this.trustedServerCertificatePemFile = trustedServerCertificatePemFile;
        return this;
    }

    public InputStream getTrustedServerCertificateInputStream() {
        return trustedServerCertificateInputStream;
    }

    public SslContextBuilder setTrustedServerCertificateInputStream(InputStream trustedServerCertificateInputStream) {
        this.trustedServerCertificateInputStream = trustedServerCertificateInputStream;
        return this;
    }

    public X509Certificate[] getTrustedServerCertificates() {
        return trustedServerCertificates;
    }

    public SslContextBuilder setTrustedServerCertificates(X509Certificate[] trustedServerCertificates) {
        this.trustedServerCertificates = trustedServerCertificates;
        return this;
    }

    /**
	 *  构建 SslContext
	 * @return
	 * @throws InvalidSSLConfig
	 */
	public SslContext build() throws InvalidSSLConfig {
		SslContext sslContext = null;
		boolean useTlsAuthentication;

		{
			SslProvider sslProvider = null;
			if (this.sslProvider != null) {
				sslProvider = this.sslProvider;
			} else {
				if (OpenSsl.isAvailable()) {
					if (OpenSsl.isAlpnSupported()) {
						logger.info(
								"Native SSL provider is available and supports ALPN; will use native provider.");
						sslProvider = SslProvider.OPENSSL;
					} else {
						logger.info(
								"Native SSL provider is available, but does not support ALPN; will use JDK SSL provider.");
						sslProvider = SslProvider.JDK;
					}
				} else {
					logger.info(
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

			useTlsAuthentication = (this.clientCertificate != null
					&& this.privateKey != null);

			if (useTlsAuthentication) {
				builder.keyManager(this.privateKey, this.privateKeyPassword,
						this.clientCertificate);
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
				logger.error("SslContext build Error:", e);
				throw new InvalidSSLConfig("SslContext build Error", e);
			}
		}

		return sslContext;
	}
}
