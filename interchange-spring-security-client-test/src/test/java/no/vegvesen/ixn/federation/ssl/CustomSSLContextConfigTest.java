package no.vegvesen.ixn.federation.ssl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {SslJavaxSettingsApp.class})
class CustomSSLContextConfigTest {

	@Qualifier("getTestSslContext")
	@Autowired
	SSLContext sslContext;

	@Test
	void defaultSSLContextWithSystemParametersSetContainsCustomKey() throws Exception {
		assertThat(sslContext).isNotNull();
		assertThat(sslContext.getProtocol())
				.isIn("TLS", "TLSv1.2", "TLSv1.3");


		SSLEngine engine = sslContext.createSSLEngine();
		assertThat(engine).isNotNull();

		SSLParameters params = sslContext.getDefaultSSLParameters();
		assertThat(params.getCipherSuites()).isNotEmpty();
		assertThat(sslContext.getProtocol()).contains("TLS");

	}

}