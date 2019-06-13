package no.vegvesen.ixn.federation;


import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;

public class FederationSystemST {

	SSLContext sourceSslContext;
	SSLContext sinkSslContext;

	@Before
	public void setUp() throws Exception {
		KeystoreDetails truststoreDetails = new KeystoreDetails("/jks/keys/truststore.jks", "password", KeystoreType.JKS);

		KeystoreDetails sourceKeystore = new KeystoreDetails("/jks/keys/remote.p12", "password", KeystoreType.PKCS12, "password");
		sourceSslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(sourceKeystore, truststoreDetails);

		KeystoreDetails sinkKeystore = new KeystoreDetails("/jks/keys/bouvet.p12", "password", KeystoreType.PKCS12, "password");
		sinkSslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(sinkKeystore, truststoreDetails);
	}

	@Test
	public void localMessageGetsForwardedAfterServiceDiscovery() {
		assertThat(sourceSslContext).isNotNull();
	}
}
