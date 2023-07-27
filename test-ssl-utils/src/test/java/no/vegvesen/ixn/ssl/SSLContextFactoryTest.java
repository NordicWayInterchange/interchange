package no.vegvesen.ixn.ssl;

import no.vegvesen.ixn.TestKeystoreHelper;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SSLContextFactoryTest {

	private Path keystorePath = Paths.get("src", "test", "resources", "jks");

	@Test
	public void sslContextFromKeyAndTrustStores() {
		TestKeystoreHelper.sslContext(keystorePath,"localhost.p12","password","truststore.jks","password");
	}

	@Test
	public void sslContextFromKeyAndTrustStoresNonExistingFileFails() {
		String keystoreFile = "/non/existing/file/foobar.p12";
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.PKCS12);

		String filePath = keystorePath.resolve("truststore.jks").toString();
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.JKS);

		assertThatExceptionOfType(SSLContextFactory.InvalidSSLConfig.class).isThrownBy(() -> {
			SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
		});
	}


}