package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class CreateKeystoreTest {


    @Test
    @Disabled
    public void generateKeystoreAndTrustStore() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException {
        Path basePath = Paths.get("src","test","resources");
        Path keyPath = basePath.resolve( "henrik2.key.pem");
        Path outputPath = Paths.get("target");
        PEMParser pemParser = new PEMParser(new FileReader(keyPath.toFile()));
        Object o = pemParser.readObject();
        PrivateKey privateKey;
        if (o instanceof PrivateKeyInfo) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            privateKey = converter.getPrivateKey((PrivateKeyInfo) o);
            System.out.println("Converted to private key");
        } else if (o instanceof PEMKeyPair){
            PEMKeyPair keyPair = (PEMKeyPair) o;
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            privateKey = converter.getPrivateKey(keyPair.getPrivateKeyInfo());

        } else {
            System.out.println(o.getClass().getName());
            throw new RuntimeException("Could not get private key, unexpected key type");
        }
        Path certPath = basePath.resolve("henrik2.crt.pem");
        pemParser = new PEMParser(new FileReader(certPath.toFile()));
        List<X509Certificate> certificates = new ArrayList<>();
        while ((o = pemParser.readObject()) != null) {
            if (o instanceof X509CertificateHolder) {
                X509CertificateHolder h = (X509CertificateHolder) o;
                X509Certificate c = new JcaX509CertificateConverter().getCertificate(h);
                certificates.add(c);
                System.out.println(String.format("%s, %s", CertSigner.getCN(h.getSubject()),CertSigner.getCN(h.getIssuer())));
            } else {
                throw new RuntimeException("Could not get certificate, unexpected type");
            }
        }
        String cn = CertSigner.getCN(new JcaX509CertificateHolder(certificates.get(0)).getSubject());
        System.out.println(cn);
        ClusterKeyGenerator.generateKeystore("password", cn,privateKey,certificates, new FileOutputStream(outputPath.resolve("henrik.p12").toFile()));

        ClusterKeyGenerator.makeTrustStore("password", "myKey",certificates.get(certificates.size() -1), new FileOutputStream(outputPath.resolve("truststore.jks").toFile()));




    }

}
