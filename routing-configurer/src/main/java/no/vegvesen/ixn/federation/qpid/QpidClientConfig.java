package no.vegvesen.ixn.federation.qpid;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

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

	private HttpClient httpsClient() throws InvalidSSLConfig {
		return HttpClients.custom()
				.setSSLContext(sslContextFromKeystoreAndTruststore())
				.build();
	}

	private SSLContext sslContextFromKeystoreAndTruststore() throws InvalidSSLConfig {
		KeyStore keystore = loadKeystore(keystoreName, keystorePassword, keystoreType);
		KeyStore truststore = loadKeystore(truststoreName, truststorePassword, truststoreType);
		return newSSLContext(keystore, keyPassword, truststore);
	}

	private static KeyStore loadKeystore(String keystoreName, String password, String keystoreType) throws InvalidSSLConfig {
		KeyStore keystore;
		try {
			keystore = KeyStore.getInstance(keystoreType);
			keystore.load(new FileInputStream(getFilePath(keystoreName)), password.toCharArray());
		} catch (Exception e) {
			throw new InvalidSSLConfig(e);
		}
		return keystore;
	}

	private static String getFilePath(String jksTestResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(jksTestResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load test jks resource " + jksTestResource);
	}

	private static SSLContext newSSLContext(final KeyStore ks, final String keyPassword, final KeyStore ts) throws InvalidSSLConfig {
		try {
			// Get a KeyManager and initialize it
			final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, keyPassword.toCharArray());

			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ts);

			// Get the SSLContext to help create SSLSocketFactory
			final SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			return sslContext;
		} catch (final GeneralSecurityException e) {
			throw new InvalidSSLConfig(e);
		}
	}

	static class InvalidSSLConfig extends Exception {
		InvalidSSLConfig(Exception e) {
			super(e);
		}
	}

	@Bean
	public RestTemplate restTemplate() throws InvalidSSLConfig {
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpsClient()));
	}
}
