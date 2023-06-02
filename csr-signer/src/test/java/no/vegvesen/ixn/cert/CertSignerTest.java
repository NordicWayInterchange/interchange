package no.vegvesen.ixn.cert;

import no.vegvesen.ixn.docker.DockerBaseIT;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CertSignerTest {

    @Test
    public void testSigningCsr() throws IOException {
        //TODO copy this to a better location
        Path path = DockerBaseIT.getTargetFolderPathForTestClass(CertSignerIT.class);
        String intermediateCaCertAsString = Files.readString(path.resolve("int.interchangetestdomain.no.crt.pem"));
        String intermediateKeyAsString = Files.readString(path.resolve("int.interchangetestdomain.no.key.pem"));

        String csrAsString = Files.readString(path.resolve("testSP.csr.pem"));
        /*
        Path chainPath = path.resolve("chain.interchangetestdomain.crt.pem");
        if (! Files.exists(chainPath)) {
            System.out.println("Does not exist for some reason");
        }
        String intermediateCertChainAsString = Files.readString(chainPath);
*/
        CertSigner signer = new CertSigner(intermediateCaCertAsString,intermediateKeyAsString);

        String CertificateAsString = signer.sign(csrAsString,"testSP");


    }
}
