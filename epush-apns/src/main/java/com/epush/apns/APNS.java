package com.epush.apns;

/**
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class APNS {

    /**
     */
    private APNS() {
    }

    /**
     * 构建一个ApnsPayload
     *
     * @return
     */
    public static ApnsPayload newPayload() {
        return new ApnsPayload();
    }

    /**
     * 通知构建器
     *
     * @return
     */
    public static ApnsPushNotification.PushNotificationBuilder newNotificationBuilder() {
        return new ApnsPushNotification.PushNotificationBuilder();
    }

    /**
     * 通知构建器
     *
     * @return
     */
    public static ApnsServiceBuilder newApnsServiceBuilder() {
        return new ApnsServiceBuilder();
    }
}
