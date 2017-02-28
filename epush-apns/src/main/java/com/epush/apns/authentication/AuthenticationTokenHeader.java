package com.epush.apns.authentication;

import com.google.gson.annotations.SerializedName;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class AuthenticationTokenHeader {

	@SerializedName("alg")
	private final String algorithm = "ES256";

	@SerializedName("typ")
	private final String tokenType = "JWT";

	@SerializedName("kid")
	private final String keyId;

	public AuthenticationTokenHeader(final String keyId) {
		this.keyId = keyId;
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	public String getTokenType() {
		return this.tokenType;
	}

	public String getKeyId() {
		return this.keyId;
	}
}
