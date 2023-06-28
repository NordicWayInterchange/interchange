package no.vegvesen.ixn.cert;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
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

    @Test
    public void testCsrByPKIjs() throws IOException {
        String csrAsString = Files.readString(Paths.get("src", "test","resources", "csr.pem"), Charset.forName("UTF-8"));
        PKCS10CertificationRequest csr = CertSigner.getPkcs10CertificationRequest(csrAsString);
        X500Name subject = csr.getSubject();
        for (RDN rdn : subject.getRDNs()) {
            System.out.println(String.format("Multivalued? %s",rdn.isMultiValued()));
            for (AttributeTypeAndValue typeAndValue : rdn.getTypesAndValues()) {
                ASN1ObjectIdentifier type = typeAndValue.getType();
                ASN1Encodable value = typeAndValue.getValue();
                if (type.equals(BCStyle.CN)) {
                    System.out.println(String.format("CN:%s, %s", type,value.toString()));
                } else if (type.equals(BCStyle.C)) {
                    System.out.println(String.format("C:%s, %s", type,value.toString()));
                } else if (type.equals(BCStyle.O)) {
                    System.out.println(String.format("O:%s, %s",type,value.toString()));
                } else {
                    System.out.println("Skipping...");
                }
            }
        }

        System.out.println("----");
        System.out.println(CertSigner.getCN(csr.getSubject()));
    }

   //Test that:
   //SP name is correct in CSR, ie the same that is passed in.
   //We use the correct extensions.

}
