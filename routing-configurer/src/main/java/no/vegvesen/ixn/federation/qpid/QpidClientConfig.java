package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.URL;

@Configuration
public class QpidClientConfig {

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

	private HttpClient httpsClient() {
		return HttpClients.custom()
				.setSSLContext(sslContextFromKeystoreAndTruststore())
				.build();
	}

	private SSLContext sslContextFromKeystoreAndTruststore() {
		String keystoreFileName = getFilePathFromClasspathResource(keystoreName);
		String truststoreFileName = getFilePathFromClasspathResource(truststoreName);
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				keystoreFileName, keystorePassword, keystoreType,
				truststoreFileName, truststorePassword, truststoreType,
				keyPassword);
	}

	private static String getFilePathFromClasspathResource(String classpathResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load classpath resource " + classpathResource);
	}

	@Bean
	public RestTemplate restTemplate() throws SSLContextFactory.InvalidSSLConfig {
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpsClient()));
	}
}
