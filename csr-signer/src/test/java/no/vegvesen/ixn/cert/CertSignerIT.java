package no.vegvesen.ixn.cert;

import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.docker.keygen.IntermediateDomain;
import no.vegvesen.ixn.docker.keygen.ServicProviderDescription;
import no.vegvesen.ixn.docker.keygen.TopDomain;
import no.vegvesen.ixn.docker.keygen.generator.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.fail;


public class CertSignerIT {

    private static Path containerOutPath = DockerBaseIT.getTargetFolderPathForTestClass(CertSignerIT.class);

    @Test
    @Disabled
    public void testFoo() {
        //generate CA, intermediate CA, certs and keys, and CSR for SP
        //TODO should have something that just gives the paths to the different things.
        //fail("This must be implemented");
        CertKeyPair rootCa = ClusterKeyGenerator.generateRootCA(
                containerOutPath,
                new TopDomain("testdomain.no", "NO")
        );
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
        //Instantiate the certsigner, give the csr, intermediate key and cert, and cert chain.
        //SPM:
        //Skal vi lage en ny container for napcore?
        /*
        CertSigner signer = new CertSigner(
                intermediateCert.getSingleCertOnHost(),
                intermediateCert.getChainCertOnHost(),
                intermediateCert.getIntermediateKeyOnHost());
*/

    }
}
