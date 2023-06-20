package no.vegvesen.ixn.cert;

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CertSignerTest {

    private Path path = Paths.get("src","test","resources");

    @Test
    public void testSigningCsr() throws IOException, OperatorCreationException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, SignatureException, InvalidKeyException, NoSuchProviderException {
        String csrAsString = Files.readString(path.resolve("testSP.csr.pem"));

        String keystoreLocation = path.resolve("keystore.p12").toString();
        String keystorePassword = "password";
        KeyStore keyStore = CertSigner.loadKeyStore(keystoreLocation,keystorePassword, "PKCS12");
        String truststoreLocation = path.resolve("truststore.jks").toString();
        KeyStore trustStore = CertSigner.loadKeyStore(truststoreLocation, "trustpassword", "JKS");
        String keyAlias = "interchangetestdomain.no";
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keystorePassword.toCharArray());
        X509Certificate intermediateCertificate = (X509Certificate) keyStore.getCertificate(keyAlias);
        X509Certificate caCertificate = (X509Certificate) trustStore.getCertificate("myKey");
        CertSigner signer = new CertSigner(privateKey,intermediateCertificate,caCertificate);
        List<String> certificatesAsString = signer.sign(csrAsString,"testSP");
        assertThat(certificatesAsString).isNotNull();
        assertThat(certificatesAsString).isNotEmpty();
        assertThat(certificatesAsString).hasSize(3);
    }

   //Test that:
   //SP name is correct in CSR, ie the same that is passed in.
   //We use the correct extensions.

}
