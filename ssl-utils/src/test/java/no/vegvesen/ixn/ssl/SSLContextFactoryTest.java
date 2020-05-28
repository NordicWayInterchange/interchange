package no.vegvesen.ixn.ssl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

public class SSLContextFactoryTest {

	@Test
	public void sslContextFromKeyAndTrustStores() {
		String keystoreFile = getFilePath("jks/localhost.p12");
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.PKCS12, "password");

		String filePath = getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.JKS);

		SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
	}

	@Test
	public void sslContextFromKeyAndTrustStoresWrongKeystoreTypeFails() {
		String keystoreFile = getFilePath("jks/localhost.p12");
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.JKS, "password");

		String filePath = getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.PKCS12);

		Assertions.assertThrows(SSLContextFactory.InvalidSSLConfig.class, () -> {
			SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
		});
	}

	@Test
	public void sslContextFromKeyAndTrustStoresNonExistingFileFails() {
		String keystoreFile = "/non/existing/file/foobar.p12";
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.PKCS12, "password");

		String filePath = getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.JKS);

		Assertions.assertThrows(SSLContextFactory.InvalidSSLConfig.class, () -> {
			SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
		});
	}


	private static String getFilePath(String jksTestResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(jksTestResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load test jks resource " + jksTestResource);
	}

}