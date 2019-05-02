package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

@Configuration
public class QpidQueueForwardingConfigurator {
    @Value("${forwarder.keystorepath}")
    private String keystorePath;

    @Value("${forwarder.keystorepassword}")
    private String keystorePassword;

    @Value("${forwarder.keystoretype}")
    private String keystoreType;

    @Value("${forwarder.truststorepath}")
    private String truststorePath;

    @Value("${forwarder.truststorepassword}")
    private String truststorePassword;

    @Value("${forwarder.truststoretype}")
    private String truststoreType;

    @Bean
    public SSLContext createSSLContext() {
        KeystoreDetails keystore = new KeystoreDetails(keystorePath,keystorePassword, KeystoreType.valueOf(keystoreType));
        KeystoreDetails trustStore = new KeystoreDetails(truststorePath,truststorePassword,KeystoreType.valueOf(truststoreType),truststorePassword);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystore,trustStore);
    }
}
