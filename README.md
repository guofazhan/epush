# epush
```
Epush is a simple and fast version of the app Java push system
```
## epush-apns

   apns 是一个ios http2 推送客户端

> -   创建一个apns推送客户端服务
```
     final ApnsService server = APNS.newApnsServiceBuilder()
					.setP8(new File("E:\\P8\\apns.p8"), "8TS34KC67Y",
							"KQH5SH3B63", "cn.10086.app")
					.setEnvironment(ApnsConfigure.Environment.PRODUCTION.name())
					.setSslProvider(SslProvider.OPENSSL).build();
```
> -   创建一个Payload
```
       APNS.newPayload().setAlertBody("Example!")
							.toPayloadStr()
```
> -   创建一个ApnsPushNotification
```
      ApnsPushNotification pushNotification = APNS
					.newNotificationBuilder()
					.setPayload(APNS.newPayload().setAlertBody("Example!")
							.toPayloadStr())
 ```
> -   推送一条消息到apns服务端
```
       server.push(pushNotification)
    
