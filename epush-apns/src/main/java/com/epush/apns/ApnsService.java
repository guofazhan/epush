package com.epush.apns;

import com.epush.apns.authentication.P8;
import com.epush.apns.exception.ApnsException;
import com.epush.apns.http2.*;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SignatureException;

/**
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public final class ApnsService
        implements Push<PushNotificationResponse, ApnsPushNotification> {
    private static final Logger logger = LoggerFactory
            .getLogger(ApnsService.class);

    /**
     * 环境信息
     */
    private String environment;

    /**
     * P8信息
     */
    private P8 p8;

    /**
     * 请求客户端
     */
    private Http2Client client;

    /**
     *
     */
    protected ApnsService() {
    }

    /**
     *
     * @param environment
     * @return
     */
    public ApnsService setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    /**
     *
     * @return
     */
    public P8 getP8() {
        return p8;
    }

    /**
     *
     * @param p8
     * @return
     */
    public ApnsService setP8(P8 p8) {
        this.p8 = p8;
        return this;
    }

    public Http2Client getClient() {
        return client;
    }

    public ApnsService setClient(Http2Client client) {
        this.client = client;
        return this;
    }

    /**
     * 单个推送
     *
     * @param pushNotification
     * @return
     * @throws ApnsException
     */
    @Override
    public PushNotificationResponse push(ApnsPushNotification pushNotification)
            throws ApnsException {
        PushNotificationResponse response = null;
        try {
            Http2Response http2Response = client
                    .request(buildHttp2Request(pushNotification));

            if (null != http2Response) {
                response = parseHttp2Response(http2Response);
                response.setPushNotification(pushNotification);
            } else {
                response = PushNotificationResponse.build(pushNotification,
                        false, "HTTP2 Response Is Empty");
            }
        } catch (Exception e) {
            logger.error("Push Error : ", e);
            response = PushNotificationResponse.build(pushNotification, false,
                    e.getMessage());
        }

        return response;
    }

    /**
     *  构建 Http2 请求
     * @param apnsPushNotification
     * @return
     * @throws SignatureException
     */
    protected Http2Request buildHttp2Request(
            ApnsPushNotification apnsPushNotification)
            throws SignatureException {
        if (logger.isDebugEnabled()) {
            logger.debug("Build Http2 Request from ApnsPushNotification {}",
                    apnsPushNotification);
        }

        ApnsConfigure.Environment environment = ApnsConfigure.Environment
                .getEnvironment(this.environment);
        final Host host = new Host(environment.getHost(),
                environment.getPort());
        // 构建一个HTTP2请求
        return apnsPushNotification.toHttp2Request(host,getP8());
    }

    /**
     * 解析HTTP2响应 报文
     * @param http2Response
     * @return
     */
    protected PushNotificationResponse parseHttp2Response(
            Http2Response http2Response) {
        if (logger.isDebugEnabled()) {
            logger.debug("Parser Http2 Response {} To PushNotificationResponse",
                    http2Response);
        }
        return PushNotificationResponse.build(http2Response);
    }

}
