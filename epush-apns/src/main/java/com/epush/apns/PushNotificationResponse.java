package com.epush.apns;

import com.epush.apns.http2.Http2Response;
import com.epush.apns.utils.DateAsTimeSerializerAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 通知返回
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class PushNotificationResponse {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Date.class, new DateAsTimeSerializerAdapter(TimeUnit.MILLISECONDS))
            .create();

    static final String EXPIRED_AUTH_TOKEN_REASON = "ExpiredProviderToken";

    /**
     * 通知详情
     */
    private ApnsPushNotification pushNotification;
    /**
     * 成功表示
     */
    private boolean success;
    /**
     * 失败原因
     */
    private String rejectionReason;
    /**
     * 失效时间
     */
    private Date tokenExpirationTimestamp;

    protected PushNotificationResponse() {
    }

    /**
     * 构建 推送响应报文
     *
     * @param http2Response
     * @return
     */
    public static PushNotificationResponse build(
            Http2Response http2Response) {
        PushNotificationResponse pushNotificationResponse = new PushNotificationResponse();
        if (http2Response.getException() != null) {
            pushNotificationResponse.setRejectionReason(http2Response.getException().getMessage());
            pushNotificationResponse.setSuccess(false);
        } else if (http2Response.getHttpResponse() != null) {
            final HttpResponseStatus status = http2Response.getHttpResponse().status();
            if (status.equals(HttpResponseStatus.OK)) {
                pushNotificationResponse.setRejectionReason("");
                pushNotificationResponse.setSuccess(true);
            } else {
                //请求失败
                pushNotificationResponse.setSuccess(false);
                final String content = http2Response.getHttpResponse().content().toString(StandardCharsets.UTF_8);
                if (content != null) {
                    final ErrorResponse errorResponse = gson.fromJson(content, ErrorResponse.class);
                    pushNotificationResponse.setRejectionReason(errorResponse.getReason());
                    pushNotificationResponse.setTokenExpirationTimestamp(errorResponse.getTimestamp());
                } else {
                    pushNotificationResponse.setRejectionReason(status.toString());
                }


            }
        }
        return pushNotificationResponse;
    }

    public static PushNotificationResponse build(
            ApnsPushNotification pushNotification) {
        return new PushNotificationResponse().setSuccess(true)
                .setPushNotification(pushNotification).setRejectionReason("")
                .setTokenExpirationTimestamp(null);
    }

    /**
     * @param success
     * @param rejectionReason
     * @return
     */
    public static PushNotificationResponse build(boolean success,
                                                 String rejectionReason) {
        return new PushNotificationResponse().setSuccess(success)
                .setPushNotification(null).setRejectionReason(rejectionReason)
                .setTokenExpirationTimestamp(null);
    }

    /**
     * @param pushNotification
     * @param success
     * @param rejectionReason
     * @return
     */
    public static PushNotificationResponse build(
            ApnsPushNotification pushNotification, boolean success,
            String rejectionReason) {
        return new PushNotificationResponse().setSuccess(success)
                .setPushNotification(pushNotification)
                .setRejectionReason(rejectionReason)
                .setTokenExpirationTimestamp(null);
    }

    /**
     * 构建推送响应
     *
     * @param pushNotification
     * @param success
     * @param rejectionReason
     * @param tokenExpirationTimestamp
     * @return
     */
    public static PushNotificationResponse build(
            ApnsPushNotification pushNotification, boolean success,
            String rejectionReason, Date tokenExpirationTimestamp) {
        return new PushNotificationResponse().setSuccess(success)
                .setPushNotification(pushNotification)
                .setRejectionReason(rejectionReason)
                .setTokenExpirationTimestamp(tokenExpirationTimestamp);
    }

    public ApnsPushNotification getPushNotification() {
        return pushNotification;
    }

    public PushNotificationResponse setPushNotification(
            ApnsPushNotification pushNotification) {
        this.pushNotification = pushNotification;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public PushNotificationResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public PushNotificationResponse setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
        return this;
    }

    public Date getTokenExpirationTimestamp() {
        return tokenExpirationTimestamp;
    }

    public PushNotificationResponse setTokenExpirationTimestamp(
            Date tokenExpirationTimestamp) {
        this.tokenExpirationTimestamp = tokenExpirationTimestamp;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("PushNotificationResponse [pushNotification=");
        builder.append(this.pushNotification);
        builder.append(", success=");
        builder.append(this.success);
        builder.append(", rejectionReason=");
        builder.append(this.rejectionReason);
        builder.append(", tokenExpirationTimestamp=");
        builder.append(this.tokenExpirationTimestamp);
        builder.append("]");
        return builder.toString();
    }

    class ErrorResponse {

        private final String reason;
        private final Date timestamp;

        public ErrorResponse(final String reason, final Date timestamp) {
            this.reason = reason;
            this.timestamp = timestamp;
        }

        public String getReason() {
            return this.reason;
        }

        public Date getTimestamp() {
            return this.timestamp;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("ErrorResponse [reason=");
            builder.append(this.reason);
            builder.append(", timestamp=");
            builder.append(this.timestamp);
            builder.append("]");
            return builder.toString();
        }
    }
}
