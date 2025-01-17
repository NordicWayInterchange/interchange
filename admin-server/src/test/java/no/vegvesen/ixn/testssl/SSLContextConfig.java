package no.vegvesen.ixn.testssl;


import no.vegvesen.ixn.federation.adminserver.TestSSLProperties;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.net.ssl.SSLContext;

@Configuration
public class SSLContextConfig {

    final
    TestSSLProperties properties;

    @Autowired
    public SSLContextConfig(TestSSLProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Primary
    public SSLContext getTestSslContext() {
        return SSLContextFactory.sslContextFromKeyAndTrustStores(
                new KeystoreDetails(properties.getKeyStore(), properties.getKeystorePassword(), KeystoreType.valueOf(properties.getKeystoreType())),
                new KeystoreDetails(properties.getTrustStore(), properties.getTruststorePassword(), KeystoreType.valueOf(properties.getTruststoreType())));
    }

}
