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
import java.net.URL;
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
        KeystoreDetails keystore = new KeystoreDetails(getFilePath(keystorePath),keystorePassword, KeystoreType.valueOf(keystoreType),keystorePassword);
        KeystoreDetails trustStore = new KeystoreDetails(getFilePath(truststorePath),truststorePassword,KeystoreType.valueOf(truststoreType));
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystore,trustStore);
    }

    private static String getFilePath(String jksTestResource) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(jksTestResource);
        if (resource != null) {
            return resource.getFile();
        }
        throw new RuntimeException("Could not load test jks resource " + jksTestResource);
    }
}
