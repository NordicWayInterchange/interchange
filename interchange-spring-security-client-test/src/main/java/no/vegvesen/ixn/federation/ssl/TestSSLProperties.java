package no.vegvesen.ixn.federation.ssl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@ConfigurationProperties(prefix ="test.ssl")
public class TestSSLProperties {

	public TestSSLProperties() {
	}

	/**
	 * Password for the truststore used in test
	 */
	private String truststorePassword = "password";
	/**
	 * Type of truststore used in test
	 */
	private String truststoreType = "JKS";
	/**
	 * Path to the truststore as seen in the classpath.
	 */
	private String trustStore;


	/**
	 * Type of keystore used in test
	 */
	private String keystoreType = "PKCS12";
	/**
	 * Path to the keystore as seen in the classpath.
	 */
	private String keyStore;

	/**
	 * Password for the keystore used in test
	 */
	private String keystorePassword = "password";

	/**
	 * Password for the key within the keystore
	 */
	private String keyPassword = "password";


	public String getTruststorePassword() {
		return truststorePassword;
	}

	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}

	public String getTruststoreType() {
		return truststoreType;
	}

	public void setTruststoreType(String truststoreType) {
		this.truststoreType = truststoreType;
	}

	public String getTrustStore() {
		return trustStore;
	}

	public void setTrustStore(String trustStore) {
		this.trustStore = trustStore;
	}

	public String getKeystoreType() {
		return keystoreType;
	}

	public void setKeystoreType(String keystoreType) {
		this.keystoreType = keystoreType;
	}

	public String getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(String keyStore) {
		this.keyStore = keyStore;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public String getKeyPassword() {
		return keyPassword;
	}

	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}

	public String getKeyStoreRuntimeFileName() {
		return getFilePathFromClasspathResource(getKeyStore());
	}

	private static String getFilePathFromClasspathResource(String classpathResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load classpath resource " + classpathResource);
	}

	public String getTrustStoreRuntimeFileName() {
		return getFilePathFromClasspathResource(getTrustStore());
	}
}
