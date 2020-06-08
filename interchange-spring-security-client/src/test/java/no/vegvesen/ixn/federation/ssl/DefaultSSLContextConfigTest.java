package no.vegvesen.ixn.federation.ssl;

import org.assertj.core.api.AbstractObjectAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TestSslAppOnly.class})
class DefaultSSLContextConfigTest {

	@Autowired
	SSLContext sslContext;

	@Test
	void defaultSSLContextIsFoundWithSystemPropertiesSettings() throws NoSuchAlgorithmException, NoSuchProviderException {
		assertThat(sslContext).isNotNull();
		AbstractObjectAssert<?, ?> extracting = assertThat(sslContext)
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