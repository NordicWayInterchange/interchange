package no.vegvesen.ixn.napcore.properties;

import no.vegvesen.ixn.cert.CertSigner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "napcore.cert-signer")
public class CertGeneratorProperties {

    private String keystoreLocation;

    private String keyStorePassword;

    private String keyAlias;

    public CertGeneratorProperties() { }

    public CertGeneratorProperties(String keystoreLocation, String keyStorePassword, String keyAlias) {
        this.keystoreLocation = keystoreLocation;
        this.keyStorePassword = keyStorePassword;
        this.keyAlias = keyAlias;
    }


    @Bean
    public CertSigner createBean() {
        try {
            KeyStore keyStore = CertSigner.loadKeyStore(keystoreLocation, keyStorePassword, "PKCS12");
            PrivateKey issuerPrivateKey = CertSigner.getKey(keyStore, keyAlias, keyStorePassword);
            X509Certificate issuerCertificate = CertSigner.getCertificate(keyStore, keyAlias);
            List<X509Certificate> certificateChain = CertSigner.getCertificateChain(keyStore, keyAlias);
            return new CertSigner(issuerPrivateKey, issuerCertificate, certificateChain);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                 UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }

    }

    public String getKeystoreLocation() {
        return keystoreLocation;
    }

    public void setKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }
}
