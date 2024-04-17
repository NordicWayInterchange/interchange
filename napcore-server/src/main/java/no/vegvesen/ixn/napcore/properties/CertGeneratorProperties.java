package no.vegvesen.ixn.napcore.properties;

import no.vegvesen.ixn.cert.CertSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

@Component
public class CertGeneratorProperties {

    @Value("#{systemProperties['server.ssl.key-store']}")
    private String keystoreLocation;

    @Value("#{systemProperties['server.ssl.key-store-password']}")
    private String keyStorePassword;

    @Value("#{systemProperties['server.ssl.key-alias']}")
    private String keyAlias;


    public String getKeystoreLocation() {
        return keystoreLocation;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }



    @Bean
    public CertSigner createBean() {
        try {
            KeyStore keyStore = CertSigner.loadKeyStore(keystoreLocation,keyStorePassword,"PKCS12");
            PrivateKey issuerPrivateKey = CertSigner.getKey(keyStore, keyAlias, keyStorePassword);
            X509Certificate issuerCertificate = CertSigner.getCertificate(keyStore, keyAlias);
            List<X509Certificate> certificateChain = CertSigner.getCertificateChain(keyStore, keyAlias);
            return new CertSigner(issuerPrivateKey,issuerCertificate,certificateChain);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                 UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }

    }

}
