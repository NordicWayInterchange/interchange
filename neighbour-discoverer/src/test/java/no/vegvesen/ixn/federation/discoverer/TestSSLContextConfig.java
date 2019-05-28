package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.net.ssl.SSLContext;
import java.net.URL;

@Configuration
@Profile("test")
public class TestSSLContextConfig {

	@Value("${neighbour.ssl.trust-store-password}")
	String truststorePassword;
	@Value("${neighbour.ssl.trust-store-type}")
	String truststoreType;
	@Value("${neighbour.ssl.trust-store}")
	String truststoreName;

	@Value("${neighbour.ssl.key-store-type}")
	String keystoreType;
	@Value("${neighbour.ssl.key-store}")
	String keystoreName;
	@Value("${neighbour.ssl.key-store-password}")
	String keystorePassword;
	@Value("${neighbour.ssl.key-password}")
	String keyPassword;

	@Bean
	public SSLContext testSslContextFromKeystoreAndTruststore() {
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
