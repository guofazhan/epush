package com.epush.apns.authentication;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Objects;

/**
 * P12相关信息
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public final class P12 {

	/**
	 * 
	 */
	private final X509Certificate clientCertificate;

	/**
	 * 
	 */
	private final PrivateKey privateKey;

	/**
	 * 
	 */
	private final String keyPassword;

	/**
	 * @param clientCertificate
	 * @param privateKey
	 * @param keyPassword
	 */
	private P12(final X509Certificate clientCertificate,
			final PrivateKey privateKey, final String keyPassword) {
		this.clientCertificate = clientCertificate;
		this.privateKey = privateKey;
		this.keyPassword = keyPassword;
	}

	public X509Certificate getClientCertificate() {
		return clientCertificate;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public String getKeyPassword() {
		return keyPassword;
	}

	public static P12 buildP12(final X509Certificate clientCertificate,
			final PrivateKey privateKey, final String keyPassword) {
		return new P12(clientCertificate, privateKey, keyPassword);
	}

	/**
	 * 构建P12相关信息
	 * 
	 * @param p12File
	 * @param p12Password
	 * @return
	 * @throws IOException
	 */
	public static P12 buildP12(final File p12File, final String p12Password)
			throws IOException {
		try (final InputStream p12InputStream = new FileInputStream(p12File)) {
			return P12.buildP12(p12InputStream, p12Password);
		}
	}

	/**
	 * 
	 * @param p12InputStream
	 * @param p12Password
	 * @return
	 * @throws IOException
	 */
	public static P12 buildP12(final InputStream p12InputStream,
			final String p12Password) throws IOException {

		try {
			final PrivateKeyEntry privateKeyEntry = getPrivateKeyEntryFromP12In(
					p12InputStream, p12Password);
			final Certificate certificate = (Certificate) privateKeyEntry
					.getCertificate();
			if (!(certificate instanceof X509Certificate)) {
				throw new KeyStoreException(
						"Found a certificate in the provided PKCS#12 file, but it was not an X.509 certificate.");
			}
			X509Certificate x509Certificate = (X509Certificate) certificate;
			PrivateKey privateKey = privateKeyEntry.getPrivateKey();
			P12.buildP12(x509Certificate, privateKey, p12Password);
		} catch (final KeyStoreException e) {
			throw new SSLException(e);
		}
		return null;
	}

	/**
	 * 根据P12文件设置信息
	 * 
	 * @param p12InputStream
	 * @param password
	 * @return
	 * @throws KeyStoreException
	 * @throws IOException
	 */
	public static PrivateKeyEntry getPrivateKeyEntryFromP12In(
			final InputStream p12InputStream, final String password)
			throws KeyStoreException, IOException {
		Objects.requireNonNull(password,
				"Password may be blank, but must not be null.");

		final KeyStore keyStore = KeyStore.getInstance("PKCS12");

		try {
			keyStore.load(p12InputStream, password.toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException e) {
			throw new KeyStoreException(e);
		}

		final Enumeration<String> aliases = keyStore.aliases();
		final KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(
				password.toCharArray());

		while (aliases.hasMoreElements()) {
			final String alias = aliases.nextElement();

			KeyStore.Entry entry;

			try {
				try {
					entry = keyStore.getEntry(alias, passwordProtection);
				} catch (final UnsupportedOperationException e) {
					entry = keyStore.getEntry(alias, null);
				}
			} catch (final UnrecoverableEntryException
					| NoSuchAlgorithmException e) {
				throw new KeyStoreException(e);
			}

			if (entry instanceof KeyStore.PrivateKeyEntry) {
				return (PrivateKeyEntry) entry;
			}
		}

		throw new KeyStoreException(
				"Key store did not contain any private key entries.");
	}

}
