package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Configuration
public class SSLContextConfig {

	@Value("${routing-configurer.ssl.trust-store-password}")
	String truststorePassword;
	@Value("${routing-configurer.ssl.trust-store-type}")
	String truststoreType;
	@Value("${routing-configurer.ssl.trust-store}")
	String truststoreName;

	@Value("${routing-configurer.ssl.key-store-type}")
	String keystoreType;
	@Value("${routing-configurer.ssl.key-store}")
	String keystoreName;
	@Value("${routing-configurer.ssl.key-store-password}")
	String keystorePassword;
	@Value("${routing-configurer.ssl.key-password}")
	String keyPassword;

	@Bean
	public SSLContext sslContextFromKeystoreAndTruststore() {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(keystoreName, keystorePassword, KeystoreType.valueOf(keystoreType), keyPassword),
				new KeystoreDetails(truststoreName, truststorePassword, KeystoreType.valueOf(truststoreType)));
	}
}
