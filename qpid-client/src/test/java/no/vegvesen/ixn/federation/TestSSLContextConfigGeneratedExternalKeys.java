package no.vegvesen.ixn.federation;


import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.net.ssl.SSLContext;

public class TestSSLContextConfigGeneratedExternalKeys {

	final
	TestSSLProperties properties;

	public TestSSLContextConfigGeneratedExternalKeys(TestSSLProperties properties) {
		this.properties = properties;
	}

	public SSLContext getTestSslContext() {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(properties.getKeyStore(), properties.getKeystorePassword(), KeystoreType.valueOf(properties.getKeystoreType())),
				new KeystoreDetails(properties.getTrustStore(), properties.getTruststorePassword(), KeystoreType.valueOf(properties.getTruststoreType())));
	}

}
