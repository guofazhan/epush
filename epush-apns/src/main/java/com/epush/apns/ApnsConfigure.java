package com.epush.apns;

import com.epush.apns.exception.ApnsException;

/**
 * ApnsConfigure
 * 
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public final class ApnsConfigure {

	/**
	 * APNS默认请求端口
	 */
	public static final int DEFAULT_APNS_PORT = 443;

	/**
	 * 生产环境apns地址
	 */
	public static final String PRODUCTION_APNS_HOST = "api.push.apple.com";

	/**
	 * 测试环境apns地址
	 */
	public static final String DEVELOPMENT_APNS_HOST = "api.development.push.apple.com";

	/**
	 * 
	 */
	public enum DeliveryPriority {

		/**
		 * <p>
		 * Indicates that the APNs server should attempt to deliver a
		 * notification immediately. Additionally, according to Apple's
		 * documentation:
		 * </p>
		 *
		 * <blockquote>
		 * <p>
		 * The push notification must trigger an alert, sound, or badge on the
		 * device. It is an error to use this priority for a push that contains
		 * only the {@code content-available} key.
		 * </p>
		 * </blockquote>
		 */
		IMMEDIATE(10),

		/**
		 * <p>
		 * Indicates that the APNs server should attempt to deliver a
		 * notification "at a time that conserves power on the device receiving
		 * it."
		 * </p>
		 */
		CONSERVE_POWER(5);

		private final int code;

		DeliveryPriority(final int code) {
			this.code = code;
		}

		protected int getCode() {
			return this.code;
		}

		protected static DeliveryPriority getFromCode(final int code) {
			for (final DeliveryPriority priority : DeliveryPriority.values()) {
				if (priority.getCode() == code) {
					return priority;
				}
			}

			throw new IllegalArgumentException(String
					.format("No delivery priority found with code %d", code));
		}
	}

	/**
	 * 环境信息
	 */
	public enum Environment {
		// 正式环境
		PRODUCTION(PRODUCTION_APNS_HOST, DEFAULT_APNS_PORT),
		// 测试环境
		DEVELOPMENT(DEVELOPMENT_APNS_HOST, DEFAULT_APNS_PORT);
		private final String host;
		private final int port;

		Environment(final String host, final int port) {
			this.host = host;
			this.port = port;
		}

		/**
		 * 根据环境名称获取环境信息
		 * 
		 * @param name
		 * @return
		 */
		public static Environment getEnvironment(String name) {
			Environment result = null;
			Environment[] arr = Environment.values();
			for (Environment temp : arr) {
				if (!temp.name().equalsIgnoreCase(name))
					continue;
				result = temp;
				break;
			}
			// 未找到给定的环境时抛出异常
			if (result == null)
				throw new ApnsException(
						"Not find this name [" + name + "] Environment");
			return result;
		}

		/**
		 * 返回当前环境的host
		 * 
		 * @return
		 */
		public String getHost() {
			return host;
		}

		/**
		 * 返回当前环境的port
		 * 
		 * @return
		 */
		public int getPort() {
			return port;
		}
	}

}
