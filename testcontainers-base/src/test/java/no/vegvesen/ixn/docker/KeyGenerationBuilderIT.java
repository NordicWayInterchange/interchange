package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.docker.keygen.Cluster;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class KeyGenerationBuilderIT {

    Path keysPath = DockerBaseIT.getTestPrefixedOutputPath(KeyGenerationBuilderIT.class);

    //TODO need to finish generation and testing with extra hosts
    @Test
    public void generate() {
        Cluster myCluster = Cluster.builder()
                .topDomain().domainName("top-domain.eu").ownerCountry("NO").done()
                .interchange()
                    .intermediateDomain().domainName("node-domain.no").ownerCountry("NO").done()
                    .additionalHost()
                        .hostName("messages.node-domain.no")
                        .done()
                    .additionalHost().hostName("onboard.node-domain.no").ownerCountry("NO").done()
                    .serviceProvider().name("service-provider-1").done()
                    .serviceProvider().name("service-provider-2").country("NO").done()
                .done()
                .done();
        Cluster cluster = myCluster;
        ClusterKeyGenerator.generateKeys(cluster, keysPath);

    }

    @Test
    @Disabled
    public void generateModelForDockerComposeRedirect() {

        Cluster cluster = Cluster.builder()
                .topDomain().domainName("bouvetinterchange.eu").ownerCountry("NO").done()
                .interchange().intermediateDomain().domainName("a.bouvetinterchange.eu").ownerCountry("NO").done()
                .serviceProvider().name("king_olav.bouvetinterchange.eu").country("NO").done()
                .done()
                .interchange().intermediateDomain().domainName("b.bouvetinterchange.eu").ownerCountry("NO").done()
                .serviceProvider().name("king_gustaf.bouvetinterchange.eu").country("SE").done()
                .done().done();
        ClusterKeyGenerator.generateKeys(cluster, keysPath);
    }

    private Cluster createSTMClusterModel() {
        Cluster cluster = Cluster.builder()
                .topDomain().domainName("stminterchange.com").ownerCountry("NO").done()
                .interchange()
                    .intermediateDomain().domainName("belle.stminterchange.com").ownerCountry("NO").done()
                .serviceProvider().name("mary@belle.stminterchange.com").country("NO").done()
                .done()
                .interchange()
                    .intermediateDomain().domainName("blomst.stminterchange.com").ownerCountry("NO").done()
                .serviceProvider().name("victoria@blomst.stminterchange.com").country("NO").done()
                .done()
                .interchange()
                    .intermediateDomain().domainName("boble.stminterchange.com").ownerCountry("NO").done()
                .serviceProvider().name("mette@boble.stminterchange.com").country("NO").done()
                .done().done();
        return cluster;


    }


}
