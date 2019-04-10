package no.vegvesen.ixn.ssl;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public class SSLContextFactory {
	public static SSLContext sslContextFromKeyAndTrustStores(String keystoreFileName, String keystorePassword, String keystoreType,
													  String truststoreFileName, String truststorePassword, String truststoreType,
													  String keyPassword) throws InvalidSSLConfig {
		KeyStore keystore = loadKeystoreFromFile(keystoreFileName, keystorePassword, keystoreType);
		KeyStore truststore = loadKeystoreFromFile(truststoreFileName, truststorePassword, truststoreType);
		return newSSLContext(keystore, keyPassword, truststore);
	}

	private static KeyStore loadKeystoreFromFile(String keystoreFileName, String keystorePassword, String keystoreType) {
		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance(keystoreType);
			keyStore.load(new FileInputStream(keystoreFileName), keystorePassword.toCharArray());
		} catch (Exception e1) {
			throw new InvalidSSLConfig(e1);
		}
		return keyStore;
	}

	private static SSLContext newSSLContext(final KeyStore ks, final String keyPassword, final KeyStore ts)  {
		try {
			// Get a KeyManager and initialize it
			final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, keyPassword.toCharArray());

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

	public static class InvalidSSLConfig extends RuntimeException {
		InvalidSSLConfig(Exception e) {
			super(e);
		}
	}
}