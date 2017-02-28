package com.epush.apns.authentication;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class AuthenticationTokenClaims {

	@SerializedName("iss")
	private final String issuer;

	@SerializedName("iat")
	private final Date issuedAt;

	public AuthenticationTokenClaims(final String issuer, final Date issuedAt) {
		this.issuer = issuer;
		this.issuedAt = issuedAt;
	}

	public String getIssuer() {
		return this.issuer;
	}

	public Date getIssuedAt() {
		return this.issuedAt;
	}
}
