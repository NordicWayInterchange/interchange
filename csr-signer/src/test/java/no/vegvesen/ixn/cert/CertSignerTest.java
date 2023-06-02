package no.vegvesen.ixn.cert;

import no.vegvesen.ixn.docker.DockerBaseIT;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;

public class CertSignerTest {

    @Test
    public void testSigningCsr() throws IOException, OperatorCreationException, CertificateException {
        //TODO copy this to a better location
        Path path = DockerBaseIT.getTargetFolderPathForTestClass(CertSignerIT.class);
        String intermediateCaCertAsString = Files.readString(path.resolve("int.interchangetestdomain.no.crt.pem"));
        String intermediateKeyAsString = Files.readString(path.resolve("int.interchangetestdomain.no.key.pem"));

        String csrAsString = Files.readString(path.resolve("testSP.csr.pem"));

        CertSigner signer = new CertSigner(intermediateCaCertAsString,intermediateKeyAsString);

        String certificateAsString = signer.sign(csrAsString,"testSP");
        assertThat(certificateAsString).isNotNull();


    }
}
