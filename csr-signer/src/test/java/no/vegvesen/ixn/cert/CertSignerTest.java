package no.vegvesen.ixn.cert;

import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.docker.keygen.IntermediateDomain;
import no.vegvesen.ixn.docker.keygen.ServicProviderDescription;
import no.vegvesen.ixn.docker.keygen.TopDomain;
import no.vegvesen.ixn.docker.keygen.generator.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;


public class CertSignerTest {

    private static Path containerOutPath = DockerBaseIT.getTargetFolderPathForTestClass(CertSignerTest.class);

    @Test
    public void testFoo() {
        //generate CA, intermediate CA, certs and keys, and CSR for SP
        //TODO should have something that just gives the paths to the different things.
        CertKeyPair rootCa = ClusterKeyGenerator.generateRootCA(
                containerOutPath,
                new TopDomain("testdomain.no", "NO")
        ).getCertKeyPairOnHost();
        IntermediateDomain interchangeDomain = new IntermediateDomain("interchangetestdomain.no", "NO");
        CsrKeyPair intermediateCsr = ClusterKeyGenerator.generateIntermediateCaCsr(
                containerOutPath,
                interchangeDomain
        ).getCsrKeyPairOnHost();
        CertChainAndKey intermediateCert = ClusterKeyGenerator.generateIntermediateCaCert(
                containerOutPath,
                interchangeDomain,
                intermediateCsr.getCsrOnHost(),
                rootCa.getCaCertOnHost(),
                rootCa.getCaKeyOnHost()
        ).getCertChainAndKeyOnHost();
        CsrKeyPair serviceProviderCsr = ClusterKeyGenerator.generateCsrForServiceProvider(containerOutPath,
                new ServicProviderDescription("testSP", "NO")).getCsrKeyPairOnHost();
        //Instanciate the certsigner, give the csr, intermediate key and cert, and cert chain.



    }
}
