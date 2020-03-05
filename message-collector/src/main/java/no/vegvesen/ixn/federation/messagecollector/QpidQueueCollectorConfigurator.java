package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Configuration
public class QpidQueueCollectorConfigurator {
    @Value("${collector.keystorepath}")
    private String keystorePath;

    @Value("${collector.keystorepassword}")
    private String keystorePassword;

    @Value("${collector.keystoretype}")
    private String keystoreType;

    @Value("${collector.truststorepath}")
    private String truststorePath;

    @Value("${collector.truststorepassword}")
    private String truststorePassword;

    @Value("${collector.truststoretype}")
    private String truststoreType;

    @Bean
    public SSLContext createSSLContext() {
        KeystoreDetails keystore = new KeystoreDetails(keystorePath,keystorePassword, KeystoreType.valueOf(keystoreType),keystorePassword);
        KeystoreDetails trustStore = new KeystoreDetails(truststorePath,truststorePassword,KeystoreType.valueOf(truststoreType));
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystore,trustStore);
    }
}
