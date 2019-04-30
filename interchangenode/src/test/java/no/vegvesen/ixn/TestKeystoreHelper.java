package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.net.ssl.SSLContext;
import java.net.URL;

public class TestKeystoreHelper {

	private static final String TEST_KEYSTORE_PASSWORD = "password";
	private static final KeystoreType TRUSTSTORE_TYPE = KeystoreType.JKS;
	private static final KeystoreType KEYSTORE_TYPE = KeystoreType.PKCS12;


	static SSLContext sslContext(String keystore, String truststore) throws SSLContextFactory.InvalidSSLConfig {
		String keystoreFilePath = getFilePath(keystore);
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFilePath, TEST_KEYSTORE_PASSWORD, KEYSTORE_TYPE, TEST_KEYSTORE_PASSWORD);

		String trustStoreFilePath = getFilePath(truststore);
		KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStoreFilePath, TEST_KEYSTORE_PASSWORD, TRUSTSTORE_TYPE);
		return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
	}

	private static String getFilePath(String jksTestResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(jksTestResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load test jks resource " + jksTestResource);
	}

}
