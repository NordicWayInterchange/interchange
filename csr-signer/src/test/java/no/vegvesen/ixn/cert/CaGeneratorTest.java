package no.vegvesen.ixn.cert;

import no.vegvesen.ixn.docker.keygen.TopDomain;
import no.vegvesen.ixn.docker.keygen.generator.CertKeyPair;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;

public class CaGeneratorTest {

    @Test
    public void testGeneratingCAUsingBouncyCastle() throws NoSuchAlgorithmException {
        Security.setProperty("crypto.policy","unlimited");
        Security.insertProviderAt(new BouncyCastleProvider(),1);
        System.out.println(Cipher.getMaxAllowedKeyLength("AES"));
        CaGenerator.generateRootCA_BC();
    }
}
