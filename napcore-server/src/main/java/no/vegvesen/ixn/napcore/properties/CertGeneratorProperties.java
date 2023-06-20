package no.vegvesen.ixn.napcore.properties;

import no.vegvesen.ixn.cert.CertSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Component
public class CertGeneratorProperties {

    @Value("#{systemProperties['server.ssl.key-store']}")
    private String keystoreLocation;

    @Value("#{systemProperties['server.ssl.key-store-password']}")
    private String keyStorePassword;

    @Value("#{systemProperties['server.ssl.key-alias']}")
    private String keyAlias;

    @Value("#{systemProperties['server.ssl.trust-store']}")
    private String truststoreLocation;

    @Value("#{systemProperties['server.ssl.trust-store-password']}")
    private String truststorePassword;

    public String getKeystoreLocation() {
        return keystoreLocation;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public String getTruststoreLocation() {
        return truststoreLocation;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }


    @Bean
    public CertSigner createBean() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(keystoreLocation),keyStorePassword.toCharArray());
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(truststoreLocation),truststorePassword.toCharArray());
            return new CertSigner(keyStore,keyAlias,keyStorePassword,trustStore,"myKey");
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                 UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }

    }
}
