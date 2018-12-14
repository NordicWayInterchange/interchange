package no.vegvesen.ixn;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public class TestKeystoreHelper {

	private static final String JAVAX_NET_SSL_KEY_STORE = "javax.net.ssl.keyStore";
	private static final String JAVAX_NET_SSL_TRUST_STORE = "javax.net.ssl.trustStore";
	private static final String TEST_KEYSTORE_PASSWORD = "password";
	private static final String TRUSTSTORE_TYPE = "JKS";
	private static final String KEYSTORE_TYPE = "PKCS12";

	public static void useTestKeystore(String userKeystore, String truststore) {
		if (userKeystore != null) {
			System.setProperty(JAVAX_NET_SSL_KEY_STORE, getFilePath(userKeystore));
			System.setProperty("javax.net.ssl.keyStorePassword", TEST_KEYSTORE_PASSWORD);
			System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
		}
		if (truststore != null) {
			System.setProperty(JAVAX_NET_SSL_TRUST_STORE, getFilePath(truststore));
			System.setProperty("javax.net.ssl.trustStorePassword", TEST_KEYSTORE_PASSWORD);
		}
	}


	static SSLContext sslContext(String keystore, String truststore) throws InvalidSSLConfig {
		return sslContextFromKeystore(getFilePath(keystore), TEST_KEYSTORE_PASSWORD.toCharArray(), getFilePath(truststore));
	}

	private static SSLContext sslContextFromKeystore(String keystoreResourcePath,
													 char[] password,
													 String truststoreResourcePath) throws InvalidSSLConfig {
		KeyStore keystore = loadKeystore(keystoreResourcePath, password, KEYSTORE_TYPE);
		KeyStore truststore = loadKeystore(truststoreResourcePath, password, TRUSTSTORE_TYPE);
		return newSSLContext(keystore, password, truststore);
	}

	private static KeyStore loadKeystore(String keystoreResourcePath, char[] password, String keystoreType) throws InvalidSSLConfig {
		KeyStore keystore;
		try {
			keystore = KeyStore.getInstance(keystoreType);
			keystore.load(new FileInputStream(keystoreResourcePath), password);
		} catch (Exception e) {
			throw new InvalidSSLConfig(e);
		}
		return keystore;
	}

	private static String getFilePath(String jksTestResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(jksTestResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load test jks resource " + jksTestResource);
	}

	private static SSLContext newSSLContext(final KeyStore ks, final char[] password, final KeyStore ts) throws InvalidSSLConfig {
		try {
			// Get a KeyManager and initialize it
			final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, password);

			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ts);

			// Get the SSLContext to help create SSLSocketFactory
			final SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			return sslContext;
		} catch (final GeneralSecurityException e) {
			throw new InvalidSSLConfig(e);
		}
	}

	static class InvalidSSLConfig extends Exception {
		InvalidSSLConfig(Exception e) {
			super(e);
		}
	}

}
