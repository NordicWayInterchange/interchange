package no.vegvesen.ixn.cert;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class CsrGenerator {

    private final KeyPairGenerator keyPairGenerator;
    private final String signAlgorithm;

    public CsrGenerator(String keyAlgorithm, int keyLength, String signAlgorithm) throws NoSuchAlgorithmException {
        keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm);
        this.signAlgorithm = signAlgorithm;
        keyPairGenerator.initialize(keyLength);
    }

    public KeyPairAndCsr generateKeyPairAndCsr(X500Name x500Name) throws OperatorCreationException {
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        JcaPKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(x500Name, keyPair.getPublic());
        JcaContentSignerBuilder signBuilder = new JcaContentSignerBuilder(signAlgorithm);
        ContentSigner signer = signBuilder.build(keyPair.getPrivate());
        PKCS10CertificationRequest csr = builder.build(signer);
        return new KeyPairAndCsr(keyPair, csr);
    }
}
