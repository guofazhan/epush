package com.epush.apns;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.util.Collection;

import javax.net.ssl.SSLException;

import com.epush.apns.authentication.P12;
import com.epush.apns.authentication.P8;
import com.epush.apns.http2.Http2Client;
import com.epush.apns.http2.proxy.ProxyHandlerFactory;

import io.netty.handler.ssl.*;


/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ApnsServiceBuilder {

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

    /**
     *
     */
    private long readAndWriterTimeout;

    private int maxContentLength;


    private static final int INITIAL_PAYLOAD_BUFFER_CAPACITY = 4096;

    /**
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

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public ApnsServiceBuilder setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
        return this;
    }

    public long getReadAndWriterTimeout() {
        return readAndWriterTimeout;
    }

    public ApnsServiceBuilder setReadAndWriterTimeout(long readAndWriterTimeout) {
        this.readAndWriterTimeout = readAndWriterTimeout;
        return this;
    }

    public P8 getP8() {
        return p8;
    }

    public ApnsServiceBuilder setP8(P8 p8) {
        this.p8 = p8;
        return this;
    }

    public P12 getP12() {
        return p12;
    }

    public ApnsServiceBuilder setP12(P12 p12) {
        this.p12 = p12;
        return this;
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

    /**
     * 构建
     *
     * @return
     */
    public ApnsService build() {
        final SslContext sslContext = new SslContextBuilder().setSslProvider(sslProvider).build();

        if (maxContentLength < 1) {
            maxContentLength = INITIAL_PAYLOAD_BUFFER_CAPACITY;
        }

        final ApnsHttp2Initializer initializer = new ApnsHttp2Initializer(sslContext, proxyHandlerFactory, maxContentLength);
        final Http2Client client = new Http2Client(null, initializer,
                readAndWriterTimeout);
        client.start();

        return new ApnsService().setClient(client).setEnvironment(environment).setP8(p8);
    }
}
