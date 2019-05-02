package no.vegvesen.ixn.federation.forwarding;

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

    private StoreDetails keystoreDetails() {
        return new StoreDetails(keystorePath,keystorePassword,keystoreType);
    }

    private StoreDetails trustStoreDetails() {
        return new StoreDetails(truststorePath,truststorePassword,truststoreType);
    }

    @Bean
    public SSLContext createSSLContext() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        return createSSLContext(keystoreDetails(),trustStoreDetails());
    }
    
    private SSLContext createSSLContext(StoreDetails keyStoreDetails, StoreDetails trustStoreDetails) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException {
        KeyStore keyStore = readStore(keyStoreDetails);
        KeyStore trustStore = readStore(trustStoreDetails);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore,keyStoreDetails.getPassword().toCharArray()); //TODO this assumes the same password for the key store as the actual keys...

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(),tmf.getTrustManagers(),null);
        return context;
    }
    
    private KeyStore readStore(StoreDetails storeDetails) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        try (InputStream keyStoreStream = Files.newInputStream(Paths.get(storeDetails.getPath())) ) {
            KeyStore keyStore = KeyStore.getInstance(storeDetails.getStoreType());
            keyStore.load(keyStoreStream,storeDetails.getPassword().toCharArray());
            return keyStore;
        }
    }
}
