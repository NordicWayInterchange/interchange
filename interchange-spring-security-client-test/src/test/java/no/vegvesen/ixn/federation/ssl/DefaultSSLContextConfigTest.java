package no.vegvesen.ixn.federation.ssl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {SslJavaxSettingsApp.class})
class DefaultSSLContextConfigTest {

	@Qualifier("defaultSslContext")
	@Autowired
	SSLContext sslContext;

	@Test
	void defaultSSLContextIsInitializedAndUsable() throws Exception {
		assertThat(sslContext).isNotNull();

		SSLEngine engine = sslContext.createSSLEngine();
		assertThat(engine).isNotNull();

		SSLParameters params = sslContext.getDefaultSSLParameters();
		assertThat(params).isNotNull();
		assertThat(params.getCipherSuites()).isNotEmpty();
		assertThat(params.getProtocols()).isNotEmpty();

		assertThat(sslContext.getSocketFactory()).isNotNull();
		assertThat(sslContext.getServerSocketFactory()).isNotNull();
	}
}