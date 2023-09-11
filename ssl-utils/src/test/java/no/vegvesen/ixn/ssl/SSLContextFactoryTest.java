package no.vegvesen.ixn.ssl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SSLContextFactoryTest {

	@Test
	public void sslContextFromKeyAndTrustStores() {
		String keystoreFile = getFilePath("jks/localhost.p12");
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.PKCS12);

		String filePath = getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.JKS);

		SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
	}

	@Test
	@Disabled
	public void sslContextFromKeyAndTrustStoresWrongKeystoreTypeFails() {
		String keystoreFile = getFilePath("jks/localhost.p12");
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.JKS);

		String filePath = getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.PKCS12);

		assertThatExceptionOfType(SSLContextFactory.InvalidSSLConfig.class).isThrownBy(() -> {
			SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
		});
	}

	@Test
	public void sslContextFromKeyAndTrustStoresNonExistingFileFails() {
		String keystoreFile = "/non/existing/file/foobar.p12";
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.PKCS12);

		String filePath = getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.JKS);

		assertThatExceptionOfType(SSLContextFactory.InvalidSSLConfig.class).isThrownBy(() -> {
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