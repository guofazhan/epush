package com.epush.apns.test;

import com.epush.apns.*;
import io.netty.handler.ssl.SslProvider;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/3/7]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class AsynPushApp {

	public static void main(String[] args) {
		final String token = "919c33d368633e51f6ef7a853d6b876333a177a0ff955ddd698fe12b5bb7b71d";
		try {
			final ApnsService server = APNS.newApnsServiceBuilder()
					.setP8(new File("E:\\P8\\apns.p8"), "8TS34KC67Y",
							"KQH5SH3B63", "cn.10086.app")
					.setEnvironment(ApnsConfigure.Environment.PRODUCTION.name())
					.setSslProvider(SslProvider.OPENSSL).build();
			ApnsPushNotification pushNotification = APNS
					.newNotificationBuilder()
					.setPayload(APNS.newPayload().setAlertBody("Example!")
							.toPayloadStr())
					.setToken(token).setTopic("cn.10086.app").build();

			List<Future<PushNotificationResponse>> list = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				list.add(server.asynPush(pushNotification));
			}

			for (Future<PushNotificationResponse> future : list) {
				try {
					System.out.println("OUT:" + future.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
