package no.vegvesen.ixn.cert;
/*
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.docker.keygen.ServicProviderDescription;
import no.vegvesen.ixn.docker.keygen.TopDomain;
import no.vegvesen.ixn.docker.keygen.generator.*;

 */
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CertSignerIT {

    //private static Path containerOutPath = DockerBaseIT.getTargetFolderPathForTestClass(CertSignerIT.class);
/*
    @Test
    @Disabled("This test is already done in CertSignerTest, should eventually be extended to include connecting to a server to check if the certs can be used")
    public void testFoo() throws IOException, CertificateException, NoSuchAlgorithmException, OperatorCreationException, KeyStoreException, UnrecoverableKeyException {
        //generate CA, intermediate CA, certs and keys, and CSR for SP
        CertKeyPair rootCa = ClusterKeyGenerator.generateRootCA(
                containerOutPath,
                new TopDomain("testdomain.no", "NO")
        );
        String truststoreName = "truststore.jks";
        String trustpassword = "trustpassword";
        ClusterKeyGenerator.generateTrustore(
                rootCa.getCaCertOnHost(),
                trustpassword,
                containerOutPath,
                truststoreName
        );
        String domainName = "interchangetestdomain.no";
        String owningCountry = "NO";
        CsrKeyPair intermediateCsr = ClusterKeyGenerator.generateIntermediateCaCsr(
                containerOutPath,
                domainName,
                owningCountry
        ).getCsrKeyPairOnHost();
        CertChainAndKey intermediateCert = ClusterKeyGenerator.generateIntermediateCaCert(
                containerOutPath,
                intermediateCsr.getCsrOnHost(),
                rootCa.getCaCertOnHost(),
                rootCa.getCaKeyOnHost(), domainName, owningCountry
        ).getCertChainAndKeyOnHost();
        String keystorePassword = "password";
        Path keystorePath = ClusterKeyGenerator.generateKeystore(
                containerOutPath,
                keystorePassword,
                "keystore.p12",
                intermediateCert.getIntermediateKeyOnHost(),
                intermediateCert.getChainCertOnHost(),
                rootCa.getCaCertOnHost(),
                domainName);
        CsrKeyPair serviceProviderCsr = ClusterKeyGenerator.generateCsrForServiceProvider(containerOutPath,
                new ServicProviderDescription("testSP", "NO")).getCsrKeyPairOnHost();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(keystorePath.toString()),keystorePassword.toCharArray());
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(containerOutPath.resolve(truststoreName).toString()),trustpassword.toCharArray());
        CertSigner signer = new CertSigner(keyStore, "interchangetestdomain.no", keystorePassword,trustStore,"myKey");
        String csrAsString = Files.readString(serviceProviderCsr.getCsrOnHost());
        List<String> certsAsString = signer.sign(csrAsString, "testSP");
        Path spCertFile = containerOutPath.resolve("testSP.crt.pem");
        Files.write(spCertFile,certsAsString);
        ClusterKeyGenerator.generateServiceProviderKeyStore(
                containerOutPath,
                new ServicProviderDescription("testSP","NO"),
                "sppassword",
                "testSP.p12",
                serviceProviderCsr.getKeyOnHost(),
                spCertFile,
                intermediateCert.getSingleCertOnHost()
        );
        assertThat(certsAsString).hasSize(3);
        assertThat(certsAsString).allMatch(s -> s.startsWith("-----BEGIN CERTIFICATE-----"));
        assertThat(certsAsString).allMatch(s -> s.endsWith("-----END CERTIFICATE-----\n"));
        //Now, we need to actually test using the certificate
    }
 */
}
