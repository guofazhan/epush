package com.epush.apns.test;

import com.epush.apns.APNS;
import com.epush.apns.ApnsConfigure;
import com.epush.apns.ApnsPushNotification;
import com.epush.apns.ApnsService;
import io.netty.handler.ssl.SslProvider;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by G2Y on 2017/3/3.
 */
public class ExampleApp {

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
					.setToken(token).setTopic("1").build();
			for (int i = 0; i < 1; i++) {
				System.out.println(server.push(pushNotification));
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
