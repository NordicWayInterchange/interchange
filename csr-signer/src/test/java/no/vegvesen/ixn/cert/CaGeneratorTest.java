package no.vegvesen.ixn.cert;

import no.vegvesen.ixn.docker.keygen.TopDomain;
import no.vegvesen.ixn.docker.keygen.generator.CertKeyPair;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;

public class CaGeneratorTest {

    @Test
    @Disabled
    public void testGeneratingCAUsingBouncyCastle() {
        Security.insertProviderAt(new BouncyCastleProvider(),0);
        CaGenerator.generateRootCA_BC();
    }
}
