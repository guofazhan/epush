package com.epush.apns.authentication;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class P8 {

	private final Map<String, Set<String>> topicsByTeamIdMap = new ConcurrentHashMap<>();

	private final Map<String, AuthenticationTokenSupplier> authenticationTokenSuppliersByTopic = new ConcurrentHashMap<>();

	private P8() {
	}

	public static P8 buildP8(File signingKeyPemFile, final String teamId,
			final String keyId, final Collection<String> topics)
			throws IOException, NoSuchAlgorithmException, InvalidKeyException {

		return buildP8(signingKeyPemFile, teamId, keyId,
				topics.toArray(new String[0]));
	}

	public static P8 buildP8(final File signingKeyPemFile, final String teamId,
			final String keyId, final String... topics)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		try (final FileInputStream signingKeyInputStream = new FileInputStream(
				signingKeyPemFile)) {
			return buildP8(signingKeyInputStream, teamId, keyId, topics);
		}
	}

	public static P8 buildP8(final InputStream signingKeyInputStream,
			final String teamId, final String keyId,
			final Collection<String> topics)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		return buildP8(signingKeyInputStream, teamId, keyId,
				topics.toArray(new String[0]));
	}

	public static P8 buildP8(final InputStream signingKeyInputStream,
			final String teamId, final String keyId, final String... topics)
			throws IOException, NoSuchAlgorithmException, InvalidKeyException {

		final ECPrivateKey signingKey;
		{
			final String base64EncodedPrivateKey;
			{
				final StringBuilder privateKeyBuilder = new StringBuilder();
				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(signingKeyInputStream));
				boolean haveReadHeader = false;
				boolean haveReadFooter = false;

				for (String line; (line = reader.readLine()) != null;) {
					if (!haveReadHeader) {
						if (line.contains("BEGIN PRIVATE KEY")) {
							haveReadHeader = true;
							continue;
						}
					} else {
						if (line.contains("END PRIVATE KEY")) {
							haveReadFooter = true;
							break;
						} else {
							privateKeyBuilder.append(line);
						}
					}
				}

				if (!(haveReadHeader && haveReadFooter)) {
					throw new IOException(
							"Could not find private key header/footer");
				}

				base64EncodedPrivateKey = privateKeyBuilder.toString();
			}

			final ByteBuf wrappedEncodedPrivateKey = Unpooled
					.wrappedBuffer(base64EncodedPrivateKey
							.getBytes(StandardCharsets.US_ASCII));

			try {
				final ByteBuf decodedPrivateKey = Base64
						.decode(wrappedEncodedPrivateKey);

				try {
					final byte[] keyBytes = new byte[decodedPrivateKey
							.readableBytes()];
					decodedPrivateKey.readBytes(keyBytes);

					final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(
							keyBytes);
					final KeyFactory keyFactory = KeyFactory.getInstance("EC");
					signingKey = (ECPrivateKey) keyFactory
							.generatePrivate(keySpec);
				} catch (final InvalidKeySpecException e) {
					throw new InvalidKeyException(e);
				} finally {
					decodedPrivateKey.release();
				}
			} finally {
				wrappedEncodedPrivateKey.release();
			}
		}

		return buildP8(signingKey, teamId, keyId, topics);
	}

	public static P8 buildP8(final ECPrivateKey signingKey, final String teamId,
			final String keyId, final String... topics)
			throws InvalidKeyException, NoSuchAlgorithmException {

		// if (!this.useTokenAuthentication) {
		// throw new IllegalStateException(
		// "Cannot register signing keys with clients that use TLS-based
		// authentication.");
		// }

		P8 p8 = new P8();
		final AuthenticationTokenSupplier tokenSupplier = new AuthenticationTokenSupplier(
				teamId, keyId, signingKey);
		final Set<String> topicSet = new HashSet<>();
		for (final String topic : topics) {
			topicSet.add(topic);
			p8.authenticationTokenSuppliersByTopic.put(topic, tokenSupplier);
		}
		p8.topicsByTeamIdMap.put(teamId, topicSet);

		return p8;
	}

	public Map<String, Set<String>> getTopicsByTeamIdMap() {
		return topicsByTeamIdMap;
	}

	public Map<String, AuthenticationTokenSupplier> getAuthenticationTokenSuppliersByTopic() {
		return authenticationTokenSuppliersByTopic;
	}
}
