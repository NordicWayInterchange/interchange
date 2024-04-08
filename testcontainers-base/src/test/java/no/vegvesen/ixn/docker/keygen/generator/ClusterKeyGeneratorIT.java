package no.vegvesen.ixn.docker.keygen.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.docker.keygen.Cluster;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator.CertificateCertificateChainAndKeys;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator.KeyPairAndCertificate;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClusterKeyGeneratorIT {


    Path targetPath = DockerBaseIT.getTargetFolderPathForTestClass(ClusterKeyGeneratorIT.class);

    @Test
    public void readClusterFromJson() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, OperatorCreationException, NoSuchProviderException, SignatureException, InvalidKeyException {
        Path jsonPath = Paths.get("src", "test", "resources", "cluster.json");
        Cluster cluster = new ObjectMapper().readValue(jsonPath.toFile(), Cluster.class);
        assertThat(cluster.getTopDomain().getDomainName()).isEqualTo("top-domain.eu");
        assertThat(cluster.getTopDomain().getIntermediateDomains()).hasSize(1);
        assertThat(cluster.getTopDomain().getIntermediateDomains().get(0).getInterchange().getServiceProviders()).hasSize(2);
        ClusterKeyGenerator.generateKeys(cluster,targetPath);
    }

    @Test
    public void readClusterFromAnotherJson() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, SignatureException, OperatorCreationException, NoSuchProviderException, InvalidKeyException {
        Path jsonPath = Paths.get("src", "test", "resources", "systemtest-cluster.json");
        Cluster cluster = new ObjectMapper().readValue(jsonPath.toFile(), Cluster.class);
        ClusterKeyGenerator.generateKeys(cluster,targetPath);
    }

   @Test
   public void testGenerateTopCaCountryWithDefaultCountryCode() throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, CertIOException {
       KeyPairAndCertificate keyPairAndCertificate = ClusterKeyGenerator.generateTopCa("mydomain.com", null, new SecureRandom());
       X500Name issuerName = new X500Name(keyPairAndCertificate.certificate().getIssuerX500Principal().getName());
       X500Name subjectName = new X500Name(keyPairAndCertificate.certificate().getSubjectX500Principal().getName());
       assertThat(getCountry(issuerName)).isEqualTo("NO");
       assertThat(getCountry(subjectName)).isEqualTo("NO");
   }

   @Test
   public void testGenerateIntermediateCaWithDefaultCountryCode() throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, CertIOException, SignatureException, InvalidKeyException, NoSuchProviderException {
       SecureRandom secureRandom = new SecureRandom();
       CertificateCertificateChainAndKeys topCa = ClusterKeyGenerator.generateTopCa_2("mydomain.com", null, secureRandom);
       CertificateCertificateChainAndKeys intermediateCa = ClusterKeyGenerator.generateIntermediateCA("childCa",null, topCa.getCertificateChain(),topCa.getCertificate(),topCa.getKeyPair().getPrivate(),secureRandom);
       List<X509Certificate> certificateChain = intermediateCa.getCertificateChain();
       assertThat(certificateChain).hasSize(2);
       X509Certificate certificate = intermediateCa.getCertificate();
       assertThat(certificateChain.get(1)).isEqualTo(certificate);
       X500Name subjectName = new X500Name(certificate.getSubjectX500Principal().getName());
       assertThat(getCountry(subjectName)).isEqualTo("NO");
   }

    private static String getCountry(X500Name name) {
        RDN[] rdNs = name.getRDNs(BCStyle.C);
        return IETFUtils.valueToString(rdNs[0].getFirst().getValue());
    }
}
