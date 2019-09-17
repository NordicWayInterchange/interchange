package no.vegvesen.ixn.ssl;

import no.vegvesen.ixn.TestKeystoreHelper;
import org.junit.Test;

public class SSLContextFactoryTest {

	@Test
	public void sslContextFromKeyAndTrustStores() {
		TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	}

	@Test(expected = SSLContextFactory.InvalidSSLConfig.class)
	public void sslContextFromKeyAndTrustStoresWrongKeystoreTypeFails() {
		String keystoreFile = TestKeystoreHelper.getFilePath("jks/localhost.p12");
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.JKS, "password");

		String filePath = TestKeystoreHelper.getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.PKCS12);

		SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
	}

	@Test(expected = SSLContextFactory.InvalidSSLConfig.class)
	public void sslContextFromKeyAndTrustStoresNonExistingFileFails() {
		String keystoreFile = "/non/existing/file/foobar.p12";
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.PKCS12, "password");

		String filePath = TestKeystoreHelper.getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.JKS);

		SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
	}


}