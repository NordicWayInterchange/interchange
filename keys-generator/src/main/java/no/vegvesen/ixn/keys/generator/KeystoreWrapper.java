package no.vegvesen.ixn.keys.generator;

import no.vegvesen.ixn.cert.CertSigner;

import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class KeystoreWrapper {

    private final KeyStore keyStore;

    public KeystoreWrapper(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public X509Certificate getCertificate(String keyAlias) throws KeyStoreException {
        return (X509Certificate) keyStore.getCertificate(keyAlias);
    }

    public PrivateKey getKey(String keyAlias, String keyStorePassword) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return (PrivateKey) keyStore.getKey(keyAlias, keyStorePassword.toCharArray());
    }

    public List<X509Certificate> getCertificateChain(String keyAlias) throws KeyStoreException {
        List<X509Certificate> certificateChain = new ArrayList<>();
        for (Certificate certificate : keyStore.getCertificateChain(keyAlias)) {
            certificateChain.add((X509Certificate) certificate);
        }
        return certificateChain;
    }

    public EntityDescription toCertificateCertificateChainAndKeys(String hostname, String keystorePassword) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        X509Certificate issuerCertificate = getCertificate(hostname);

        return new EntityDescription(
                new KeyPair(
                        issuerCertificate.getPublicKey(),
                        getKey(hostname, keystorePassword)
                ),
                issuerCertificate,
                getCertificateChain(hostname));
    }

    public static KeystoreWrapper loadKeystore(Path keystore, String keystorePassword, String storeType) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        return new KeystoreWrapper(CertSigner.loadKeyStore(keystore.toString(), keystorePassword, storeType));
    }


}
