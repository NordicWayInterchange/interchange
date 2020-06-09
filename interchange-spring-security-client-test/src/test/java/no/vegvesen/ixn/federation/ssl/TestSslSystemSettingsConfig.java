package no.vegvesen.ixn.federation.ssl;


import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

/**
 * Not a preferred configuration in unit tests or integration tests.
 * Use @see TestSSLContextConfig in the tests.
 *
 * This config class is used to demonstrate that javax.net.ssl system settings
 * will direct the SSLContext.getDefault() as expected.
 *
 * The javax.net.ssl system settings will be used in docker containers when operating as an ssl client.
 */
@Configuration
@ConfigurationPropertiesScan
public class TestSslSystemSettingsConfig {

	final TestSSLProperties properties;

	public TestSslSystemSettingsConfig(TestSSLProperties properties) {
		this.properties = properties;
	}

	public void setSystemSettingsForTest() {
		System.setProperty("javax.net.ssl.keyStore", properties.getKeyStoreRuntimeFileName());
		System.setProperty("javax.net.ssl.keyStorePassword", properties.getKeystorePassword());
		System.setProperty("javax.net.ssl.keyStoreType", properties.getKeystoreType());

		System.setProperty("javax.net.ssl.trustStore", properties.getTrustStoreRuntimeFileName());
		System.setProperty("javax.net.ssl.trustStorePassword", properties.getTruststorePassword());
	}



}
