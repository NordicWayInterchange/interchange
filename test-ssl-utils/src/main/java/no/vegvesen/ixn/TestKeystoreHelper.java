package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.net.ssl.SSLContext;
import java.net.URL;
import java.nio.file.Path;

public class TestKeystoreHelper {

	public static final String TEST_KEYSTORE_PASSWORD = "password";
	private static final KeystoreType TRUSTSTORE_TYPE = KeystoreType.JKS;
	private static final KeystoreType KEYSTORE_TYPE = KeystoreType.PKCS12;


	public static SSLContext sslContext(Path keystorePath, String keystore, String keystorePasssword, String truststore, String truststorePassword) throws SSLContextFactory.InvalidSSLConfig {
		String keystoreFilePath = keystorePath.resolve(keystore).toString();
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFilePath, keystorePasssword, KEYSTORE_TYPE);

		String trustStoreFilePath = keystorePath.resolve(truststore).toString();
		KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStoreFilePath, truststorePassword, TRUSTSTORE_TYPE);
		return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
	}

	public static SSLContext sslContext(String keystoreFilePath, String trustStoreFilePath) throws SSLContextFactory.InvalidSSLConfig {
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFilePath, TEST_KEYSTORE_PASSWORD, KEYSTORE_TYPE);

		KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStoreFilePath, TEST_KEYSTORE_PASSWORD, TRUSTSTORE_TYPE);
		return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
	}

	public static String getFilePath(String jksTestResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(jksTestResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load test jks resource " + jksTestResource);
	}

}
