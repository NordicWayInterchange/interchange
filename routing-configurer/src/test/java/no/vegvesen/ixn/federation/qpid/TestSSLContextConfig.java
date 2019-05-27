package no.vegvesen.ixn.federation.qpid;


import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.net.URL;

@Configuration
public class TestSSLContextConfig {

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
	public SSLContext getTestSslContext() {
		String keystoreFileName = getFilePathFromClasspathResource(keystoreName);
		String truststoreFileName = getFilePathFromClasspathResource(truststoreName);
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(keystoreFileName, keystorePassword, KeystoreType.valueOf(keystoreType), keyPassword),
			new KeystoreDetails(truststoreFileName, truststorePassword, KeystoreType.valueOf(truststoreType)));
}

	private static String getFilePathFromClasspathResource(String classpathResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load classpath resource " + classpathResource);
	}


}
