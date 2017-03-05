package com.epush.apns;

import java.util.Date;
import java.util.Objects;

/**
 * PushNotification
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ApnsPushNotification {

    /**
     *
     */
    private final String token;
    /**
     *
     */
    private final String payload;
    /**
     *
     */
    private final Date invalidationTime;
    /**
     *
     */
    private final ApnsConfigure.DeliveryPriority priority;
    /**
     *
     */
    private final String topic;
    /**
     *
     */
    private final String collapseId;

    /**
     * @param token
     * @param topic
     * @param payload
     */
    private ApnsPushNotification(final String token, final String topic,
                                 final String payload) {
        this(token, topic, payload, null,
                ApnsConfigure.DeliveryPriority.IMMEDIATE, null);
    }

    /**
     * @param token
     * @param topic
     * @param payload
     * @param invalidationTime
     */
    private ApnsPushNotification(final String token, final String topic,
                                 final String payload, final Date invalidationTime) {
        this(token, topic, payload, invalidationTime,
                ApnsConfigure.DeliveryPriority.IMMEDIATE, null);
    }

    /**
     * @param token
     * @param topic
     * @param payload
     * @param invalidationTime
     * @param priority
     * @param collapseId
     */
    private ApnsPushNotification(final String token, final String topic,
                                 final String payload, final Date invalidationTime,
                                 final ApnsConfigure.DeliveryPriority priority,
                                 final String collapseId) {
        this.token = token;
        this.payload = payload;
        this.invalidationTime = invalidationTime;
        this.priority = priority;
        this.topic = topic;
        this.collapseId = collapseId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.invalidationTime == null) ? 0
                : this.invalidationTime.hashCode());
        result = prime * result
                + ((this.payload == null) ? 0 : this.payload.hashCode());
        result = prime * result
                + ((this.priority == null) ? 0 : this.priority.hashCode());
        result = prime * result
                + ((this.token == null) ? 0 : this.token.hashCode());
        result = prime * result
                + ((this.topic == null) ? 0 : this.topic.hashCode());
        result = prime * result
                + ((this.collapseId == null) ? 0 : this.collapseId.hashCode());
        return result;
    }

    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ApnsPushNotification)) {
            return false;
        }
        final ApnsPushNotification other = (ApnsPushNotification) obj;
        if (this.invalidationTime == null) {
            if (other.invalidationTime != null) {
                return false;
            }
        } else if (!this.invalidationTime.equals(other.invalidationTime)) {
            return false;
        }
        if (this.payload == null) {
            if (other.payload != null) {
                return false;
            }
        } else if (!this.payload.equals(other.payload)) {
            return false;
        }
        if (this.priority != other.priority) {
            return false;
        }
        if (this.token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!this.token.equals(other.token)) {
            return false;
        }
        if (this.topic == null) {
            if (other.topic != null) {
                return false;
            }
        } else if (!this.topic.equals(other.topic)) {
            return false;
        }
        if (Objects.equals(this.collapseId, null)) {
            if (!Objects.equals(other.collapseId, null)) {
                return false;
            }
        } else if (!this.collapseId.equals(other.collapseId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ApnsPushNotification [token=");
        builder.append(this.token);
        builder.append(", payload=");
        builder.append(this.payload);
        builder.append(", invalidationTime=");
        builder.append(this.invalidationTime);
        builder.append(", priority=");
        builder.append(this.priority);
        builder.append(", topic=");
        builder.append(this.topic);
        builder.append(", apns-collapse-id=");
        builder.append(this.collapseId);
        builder.append("]");
        return builder.toString();
    }

    public String getToken() {
        return token;
    }

    public String getCollapseId() {
        return collapseId;
    }

    public String getPayload() {
        return payload;
    }

    public Date getInvalidationTime() {
        return invalidationTime;
    }

    public ApnsConfigure.DeliveryPriority getPriority() {
        return priority;
    }

    public String getTopic() {
        return topic;
    }

    /**
     * 通知构建器
     */
    public static class PushNotificationBuilder {

        /**
         *
         */
        private String token;
        /**
         *
         */
        private String payload;
        /**
         *
         */
        private String topic;

        protected PushNotificationBuilder() {
        }

        public PushNotificationBuilder setToken(String token) {
            this.token = token;
            return this;
        }

        public PushNotificationBuilder setPayload(String payload) {
            this.payload = payload;
            return this;
        }

        public PushNotificationBuilder setTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public ApnsPushNotification build() {
            return new ApnsPushNotification(this.token, this.topic,
                    this.payload);
        }
    }
}
