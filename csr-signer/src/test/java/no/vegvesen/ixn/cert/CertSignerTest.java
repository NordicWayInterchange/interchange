package no.vegvesen.ixn.cert;

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;

public class CertSignerTest {

    private Path path = Paths.get("src","test","resources");

    @Test
    public void testSigningCsr() throws IOException, OperatorCreationException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        String csrAsString = Files.readString(path.resolve("testSP.csr.pem"));

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(path.resolve("keystore.p12").toString()),"password".toCharArray());

        CertSigner signer = new CertSigner(keyStore,"interchangetestdomain.no","password");

        String certificateAsString = signer.sign(csrAsString,"testSP");
        assertThat(certificateAsString).isNotNull();
        assertThat(certificateAsString).isNotEmpty();
        assertThat(certificateAsString).startsWith("-----BEGIN CERTIFICATE-----");
    }

    @Test
    public void pemFromTruststore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(path.resolve("truststore.jks").toString()),"trustpassword".toCharArray());
        X509Certificate caCertificate = (X509Certificate) trustStore.getCertificate("myKey");

        String certAsString = CertSigner.certificatesToString(caCertificate);
        assertThat(certAsString).startsWith("-----BEGIN CERTIFICATE-----");
    }

   //Test that:
   //SP name is correct in CSR, ie the same that is passed in.
   //We use the correct extensions.

}
