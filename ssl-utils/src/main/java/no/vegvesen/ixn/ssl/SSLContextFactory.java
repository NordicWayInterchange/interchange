package no.vegvesen.ixn.ssl;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@SuppressWarnings("WeakerAccess")
public class SSLContextFactory {
	public static SSLContext sslContextFromKeyAndTrustStores(KeystoreDetails keystoreDetails,
															 KeystoreDetails truststoreDetails){
		KeyStore keystore = null;
		try {
			InputStream stream = new FileInputStream(keystoreDetails.getFileName());
			System.out.println(keystoreDetails.getPassword());
			keystore = loadKeystoreFromStream(stream, keystoreDetails.getType(), keystoreDetails.getPassword());
		} catch (FileNotFoundException e) {
			throw new InvalidSSLConfig("Could not load keystore",e);
		}
		KeyStore truststore = null;
		try {
			InputStream stream = new FileInputStream(truststoreDetails.getFileName());
			truststore = loadKeystoreFromStream(stream, truststoreDetails.getType(), truststoreDetails.getPassword());
		} catch (FileNotFoundException e) {
			throw new InvalidSSLConfig("Could not load truststore",e);
		}
		return newSSLContext(keystore, keystoreDetails.getPassword(), truststore);
	}

	private static KeyStore loadKeystoreFromStream(InputStream stream, KeystoreType type, String password) {
		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance(type.toString());
		} catch (KeyStoreException e) {
			throw new InvalidSSLConfig("Could not get Keystore instance",e);
		}
		try {
			keyStore.load(stream, password.toCharArray());
		} catch (IOException | NoSuchAlgorithmException | CertificateException e) {
			throw new InvalidSSLConfig("Could not load keystore",e);
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
			final SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			return sslContext;
		} catch (final GeneralSecurityException e) {
			throw new InvalidSSLConfig(e);
		}
	}

	public static class InvalidSSLConfig extends RuntimeException {
		InvalidSSLConfig(String message, Throwable t) {
			super(message,t);
		}

		InvalidSSLConfig(Throwable e) {
			super(e);
		}
	}
}