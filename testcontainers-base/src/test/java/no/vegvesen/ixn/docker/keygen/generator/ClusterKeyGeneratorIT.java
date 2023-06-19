package no.vegvesen.ixn.docker.keygen.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.docker.keygen.Cluster;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

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
}
