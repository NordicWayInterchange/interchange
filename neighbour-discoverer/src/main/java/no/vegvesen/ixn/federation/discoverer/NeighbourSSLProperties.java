package no.vegvesen.ixn.federation.discoverer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix ="neighbour.ssl")
public class NeighbourSSLProperties {

	private String trustStorePassword;
	private String trustStoreType;
	private String trustStore;
	private String keyStoreType;
	private String keyStore;
	private String keyStorePassword;
	private String keyPassword;


	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	public String getTrustStoreType() {
		return trustStoreType;
	}

	public void setTrustStoreType(String trustStoreType) {
		this.trustStoreType = trustStoreType;
	}

	public String getKeyStoreType() {
		return keyStoreType;
	}

	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public String getKeyPassword() {
		return keyPassword;
	}

	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}

	public String getTrustStore() {
		return trustStore;
	}

	public void setTrustStore(String trustStore) {
		this.trustStore = trustStore;
	}

	public String getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(String keyStore) {
		this.keyStore = keyStore;
	}

	@Override
	public String toString() {
		return "NeighbourSSLProperties{" +
				"trustStorePassword='" + trustStorePassword + '\'' +
				", trustStoreType='" + trustStoreType + '\'' +
				", trustStore='" + trustStore + '\'' +
				", keyStoreType='" + keyStoreType + '\'' +
				", keyStore='" + keyStore + '\'' +
				", keyStorePassword='" + keyStorePassword + '\'' +
				", keyPassword='" + keyPassword + '\'' +
				'}';
	}
}
