package no.vegvesen.ixn.federation.ssl;


import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Configuration
@ConfigurationPropertiesScan
public class TestSSLContextConfig {

	final
	TestSSLProperties properties;

	@Autowired
	public TestSSLContextConfig(TestSSLProperties properties) {
		this.properties = properties;
	}

	@Bean
	public SSLContext getTestSslContext() {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(properties.getKeyStoreRuntimeFileName(), properties.getKeystorePassword(), KeystoreType.valueOf(properties.getKeystoreType()), properties.getKeyPassword()),
				new KeystoreDetails(properties.getTrustStoreRuntimeFileName(), properties.getTruststorePassword(), KeystoreType.valueOf(properties.getTruststoreType())));
	}

}
