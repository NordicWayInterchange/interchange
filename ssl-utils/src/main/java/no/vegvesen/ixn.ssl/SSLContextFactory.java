package no.vegvesen.ixn.ssl;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

@SuppressWarnings("WeakerAccess")
public class SSLContextFactory {
	public static SSLContext sslContextFromKeyAndTrustStores(KeystoreDetails keystoreDetails,
															 KeystoreDetails truststoreDetails){
		KeyStore keystore = loadKeystoreFromFile(keystoreDetails);
		KeyStore truststore = loadKeystoreFromFile(truststoreDetails);
		return newSSLContext(keystore, keystoreDetails.getKeyPassword(), truststore);
	}

	private static KeyStore loadKeystoreFromFile(KeystoreDetails details) {
		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance(details.getType().toString());
			keyStore.load(new FileInputStream(details.getFileName()), details.getPassword().toCharArray());
		} catch (Throwable e1) {
			throw new InvalidSSLConfig(e1);
		}
		return keyStore;
	}

	private static SSLContext newSSLContext(final KeyStore ks, final String keyPassword, final KeyStore ts) {
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
		InvalidSSLConfig(Throwable e) {
			super(e);
		}
	}
}