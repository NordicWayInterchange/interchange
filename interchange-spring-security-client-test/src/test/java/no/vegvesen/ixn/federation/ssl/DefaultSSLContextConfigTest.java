package no.vegvesen.ixn.federation.ssl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {SslJavaxSettingsApp.class})
class DefaultSSLContextConfigTest {

	@Qualifier("defaultSslContext")
	@Autowired
	SSLContext sslContext;

	@Disabled("Extracting private fields is restricted in Java > 8")
	@Test
	void defaultSSLContextWithSystemParametersSetContainsCustomKey()  {
		assertThat(sslContext).isNotNull();
		assertThat(sslContext)
				.extracting("contextSpi")
				.extracting("keyManager")
				.isNotNull()
				.extracting("credentialsMap")
				.isNotNull()
				.extracting("routing_configurer")
				.withFailMessage("the private key routing_configurer was not loaded as expected")
				.isNotNull();
	}
}