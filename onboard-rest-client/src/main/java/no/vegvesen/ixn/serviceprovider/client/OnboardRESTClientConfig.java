package no.vegvesen.ixn.serviceprovider.client;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

//@Configuration
public class OnboardRESTClientConfig {


    @Value("${onboardclient.keystorepath}")
    private String keyStorePath;
    @Value("${onboardclient.keystorepassword}")
    private String keyStorePassword;

    @Value("${onboardclient.keystoretype}")
    private String keyStoreType;

    @Value("${onboardclient.keypassword}")
    private String keyPassword;

    @Value("${onboardclient.truststorepath}")
    private String trustStorePath;

    @Value("${onboardclient.truststorepassword}")
    private String trustStorePassword;

    @Value("${onboardclient.truststoretype}")
    private String trustStoreType;

    @Bean
    public SSLContext createSSLContext() {
        return SSLContextFactory.sslContextFromKeyAndTrustStores(
                new KeystoreDetails(keyStorePath, keyStorePassword, KeystoreType.valueOf(keyStoreType),keyPassword),
                new KeystoreDetails(trustStorePath, trustStorePassword, KeystoreType.valueOf(trustStoreType)));
    }
}
