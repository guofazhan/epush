package com.epush.apns.test;

import com.epush.apns.APNS;
import com.epush.apns.ApnsService;
import io.netty.handler.ssl.SslProvider;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/3/1]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ApnsTest {

	public static void main(String[] args)
			throws NoSuchAlgorithmException, InvalidKeyException, IOException {
		ApnsService service = APNS.newApnsServiceBuilder()
				.setP8(new File("E:\\p8\\apns.p8"), "8TS34KC67Y", "KQH5SH3B63",
						"cn.10086.app")
				.setEnvironment("PRODUCTION")
				.setSslProvider(SslProvider.OPENSSL_REFCNT).build();
		String token = "bb68b646a55b70e2fe9bef2f85d200cababb12b25d27a8401ce6a23334d0e49b";
		service.push(APNS.newNotificationBuilder()
				.setPayload(APNS.newPayload().setAlertBody("测试").toPayloadStr())
				.setToken(token).setTopic("cn.10086.app").build());

	}
}
