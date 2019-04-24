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

	@Value("${qpid.rest.api.truststore.password}")
	String truststorePassword;
	@Value("${qpid.rest.api.truststore.type}")
	String truststoreType;
	@Value("${qpid.rest.api.truststore}")
	String truststoreName;

	@Value("${qpid.rest.api.keystore.type}")
	String keystoreType;
	@Value("${qpid.rest.api.keystore}")
	String keystoreName;
	@Value("${qpid.rest.api.keystore.password}")
	String keystorePassword;
	@Value("${qpid.rest.api.keystore.key.password}")
	String keyPassword;

	@Bean
	public SSLContext sslContextFromKeystoreAndTruststore() {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(keystoreName, keystorePassword, KeystoreType.valueOf(keystoreType), keyPassword),
				new KeystoreDetails(truststoreName, truststorePassword, KeystoreType.valueOf(truststoreType)));
	}
}
