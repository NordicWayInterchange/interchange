package no.vegvesen.ixn.cert;

import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.docker.keygen.IntermediateDomain;
import no.vegvesen.ixn.docker.keygen.ServicProviderDescription;
import no.vegvesen.ixn.docker.keygen.TopDomain;
import no.vegvesen.ixn.docker.keygen.generator.*;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;


public class CertSignerIT {

    private static Path containerOutPath = DockerBaseIT.getTargetFolderPathForTestClass(CertSignerIT.class);

    @Test
    public void testFoo() throws IOException, CertificateException, NoSuchAlgorithmException, OperatorCreationException {
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
        String intermediateCertAsString = Files.readString(intermediateCert.getSingleCertOnHost());
        String intermediateKeyAsString = Files.readString(intermediateCert.getIntermediateKeyOnHost());
        CertSigner signer = new CertSigner(intermediateCertAsString,intermediateKeyAsString);
        String csrAsString = Files.readString(serviceProviderCsr.getCsrOnHost());
        String certAsString = signer.sign(csrAsString, "testSP");
        assertThat(certAsString).startsWith("-----BEGIN CERTIFICATE-----");
        //TODO should return the chain!
    }
}
