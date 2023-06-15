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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CertSignerTest {

    private Path path = Paths.get("src","test","resources");

    @Test
    public void testSigningCsr() throws IOException, OperatorCreationException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        String csrAsString = Files.readString(path.resolve("testSP.csr.pem"));

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(path.resolve("keystore.p12").toString()),"password".toCharArray());
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(path.resolve("truststore.jks").toString()),"trustpassword".toCharArray());
        CertSigner signer = new CertSigner(keyStore,"interchangetestdomain.no","password",trustStore,"myKey");

        List<String> certificatesAsString = signer.sign(csrAsString,"testSP");
        assertThat(certificatesAsString).isNotNull();
        assertThat(certificatesAsString).isNotEmpty();
        assertThat(certificatesAsString).hasSize(3);
    }

   //Test that:
   //SP name is correct in CSR, ie the same that is passed in.
   //We use the correct extensions.

}
