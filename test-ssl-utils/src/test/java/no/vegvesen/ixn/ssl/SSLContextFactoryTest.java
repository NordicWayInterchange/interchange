package no.vegvesen.ixn.ssl;

import no.vegvesen.ixn.TestKeystoreHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Disabled
public class SSLContextFactoryTest {

	@Test
	public void sslContextFromKeyAndTrustStores() {
		TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	}

	@Test
	@Disabled("This does not throw an exception in java 11 for some reason.")
	public void sslContextFromKeyAndTrustStoresWrongKeystoreTypeFails() {
		String keystoreFile = TestKeystoreHelper.getFilePath("jks/localhost.p12");
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.JKS, "password");

		String filePath = TestKeystoreHelper.getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.PKCS12);

		assertThatExceptionOfType(SSLContextFactory.InvalidSSLConfig.class).isThrownBy(() -> {
			SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
		});
	}

	@Test
	public void sslContextFromKeyAndTrustStoresNonExistingFileFails() {
		String keystoreFile = "/non/existing/file/foobar.p12";
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.PKCS12, "password");

		String filePath = TestKeystoreHelper.getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.JKS);

		assertThatExceptionOfType(SSLContextFactory.InvalidSSLConfig.class).isThrownBy(() -> {
			SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
		});
	}


}