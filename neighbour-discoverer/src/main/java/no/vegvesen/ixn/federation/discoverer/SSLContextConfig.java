package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Configuration
public class SSLContextConfig {

	private NeighbourSSLProperties sslProperties;

	@Autowired
	public SSLContextConfig(NeighbourSSLProperties sslProperties){
		this.sslProperties = sslProperties;
	}

	@Bean
	public SSLContext sslContextFromKeystoreAndTruststore() {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(sslProperties.getKeyStore(), sslProperties.getKeyStorePassword(), KeystoreType.valueOf(sslProperties.getKeyStoreType()),sslProperties.getKeyPassword()),
				new KeystoreDetails(sslProperties.getTrustStore(), sslProperties.getTrustStorePassword(), KeystoreType.valueOf(sslProperties.getTrustStoreType())));
	}
}
