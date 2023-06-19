package no.vegvesen.ixn.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.docker.keygen.Cluster;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;


public class KeyGenerationBuilderIT {

    Path keysPath = DockerBaseIT.getTestPrefixedOutputPath(KeyGenerationBuilderIT.class);

    //TODO need to finish generation and testing with extra hosts
    @Test
    @Disabled
    public void generate() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, OperatorCreationException, NoSuchProviderException, SignatureException, InvalidKeyException {
        Cluster myCluster = Cluster.builder()
                .topDomain().domainName("top-domain.eu").ownerCountry("NO")
                .intermediateDomain().domainName("node-domain.no").ownerCountry("NO")
                    .interchange()
                        .additionalHost()
                            .hostName("messages.node-domain.no").done()
                        .additionalHost()
                            .hostName("onboard.node-domain.no")
                            .ownerCountry("NO")
                        .done()
                        .serviceProvider()
                            .name("service-provider-1")
                        .done()
                        .serviceProvider()
                            .name("service-provider-2")
                            .country("NO")
                        .done()
                    .done()
                .done()
                .done()
                .done();
        Cluster cluster = myCluster;
        ClusterKeyGenerator.generateKeys(cluster, keysPath);

    }

    @Test
    public void createClusterJson() throws IOException {
        Cluster myCluster = Cluster.builder()
                .topDomain().domainName("top-domain.eu").ownerCountry("NO")
                .intermediateDomain().domainName("node-domain.no").ownerCountry("NO")
                .interchange()
                .additionalHost()
                .hostName("messages.node-domain.no")
                .done()
                .additionalHost().hostName("onboard.node-domain.no").ownerCountry("NO").done()
                .serviceProvider().name("service-provider-1").done()
                .serviceProvider().name("service-provider-2").country("NO").done()
                .done()
                .done()
                .done()
                .done();
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(myCluster));
    }



    @Test
    @Disabled
    public void generateModelForDockerComposeRedirect() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, OperatorCreationException, NoSuchProviderException, SignatureException, InvalidKeyException {

        Cluster cluster = Cluster.builder()
                .topDomain().domainName("bouvetinterchange.eu").ownerCountry("NO")
                .intermediateDomain().domainName("a.bouvetinterchange.eu").ownerCountry("NO")
                    .interchange()
                        .serviceProvider().name("king_olav.bouvetinterchange.eu").country("NO").done()
                    .done()
                .done()
                .intermediateDomain().domainName("b.bouvetinterchange.eu").ownerCountry("NO")
                    .interchange()
                        .serviceProvider().name("king_gustaf.bouvetinterchange.eu").country("SE").done()
                    .done()
                .done()
                .done().done();
        ClusterKeyGenerator.generateKeys(cluster, keysPath);
    }

    private Cluster createSTMClusterModel() {
        Cluster cluster = Cluster.builder()
                .topDomain().domainName("stminterchange.com").ownerCountry("NO")
                .intermediateDomain().domainName("belle.stminterchange.com").ownerCountry("NO")
                    .interchange()
                        .serviceProvider().name("mary@belle.stminterchange.com").country("NO").done()
                    .done()
                .done()
                .intermediateDomain().domainName("blomst.stminterchange.com").ownerCountry("NO")
                    .interchange()
                        .serviceProvider().name("victoria@blomst.stminterchange.com").country("NO").done()
                    .done()
                .done()
                .intermediateDomain().domainName("boble.stminterchange.com").ownerCountry("NO")
                    .interchange()
                        .serviceProvider().name("mette@boble.stminterchange.com").country("NO").done()
                    .done()
                .done()
                .done().done();
        return cluster;
    }


    @Test
    public void testNewStructure() {
        Cluster cluster = Cluster.builder()
                .topDomain().domainName("top-domain.com").ownerCountry("NO")
                    .intermediateDomain().domainName("interchange.domain.com").ownerCountry("NO")
                        .interchange()
                            .serviceProvider().name("test-sp").country("NO")
                            .done()
                            .additionalHost().hostName("additionalhost.interchange.domain.com").ownerCountry("NO")
                            .done()
                        .done()
                    .done()
                .done()
                .done();

    }

}
