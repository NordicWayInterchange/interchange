package no.vegvesen.ixn.federation.ssl;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

@Configuration
public class TestSslSystemSettingsConfig {

	@Value("${test.ssl.trust-store-password}")
	String truststorePassword;
	@Value("${test.ssl.trust-store-type}")
	String truststoreType;
	@Value("${test.ssl.trust-store}")
	String truststoreName;

	@Value("${test.ssl.key-store-type}")
	String keystoreType;
	@Value("${test.ssl.key-store}")
	String keystoreName;
	@Value("${test.ssl.key-store-password}")
	String keystorePassword;
	@Value("${test.ssl.key-password}")
	String keyPassword;

	public void setSystemSettingsForTest() {
		String keystoreFileName = getFilePathFromClasspathResource(keystoreName);
		System.setProperty("javax.net.ssl.keyStore", keystoreFileName);
		System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
		System.setProperty("javax.net.ssl.keyStoreType", keystoreType);

		String truststoreFileName = getFilePathFromClasspathResource(truststoreName);
		System.setProperty("javax.net.ssl.trustStore", truststoreFileName);
		System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
	}


	private static String getFilePathFromClasspathResource(String classpathResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load classpath resource " + classpathResource);
	}


}
