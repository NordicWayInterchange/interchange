package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.docker.keygen.AdditionalHost;
import no.vegvesen.ixn.docker.keygen.Cluster;
import no.vegvesen.ixn.docker.keygen.Interchange;
import no.vegvesen.ixn.docker.keygen.IntermediateDomain;
import no.vegvesen.ixn.docker.keygen.ServicProviderDescription;
import no.vegvesen.ixn.docker.keygen.TopDomain;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class KeyGenerationBuilderIT {

    Path keysPath = DockerBaseIT.getTestPrefixedOutputPath(KeyGenerationBuilderIT.class);

    @Test
    public void generate() {
        Cluster cluster = createClusterModel();
        Path imageBaseFolder = DockerBaseIT.getFolderPath("keymaster");
        generateKeys(cluster, imageBaseFolder);

    }

    @Test
    public void generateModelForRedirectSP() {

        Cluster cluster = createClusterForSPRedirect();
        generateKeys(cluster,DockerBaseIT.getFolderPath("keymaster"));
    }

    @Test
    public void generateModelForDockerComposeRedirect() {
        Cluster cluster = createClusterForSystemtest();
        generateKeys(cluster,DockerBaseIT.getFolderPath("keymaster"));
    }

    private void generateKeys(Cluster cluster, Path imageBaseFolder) {
        TopDomain topDomain = cluster.getTopDomain();
        RootCAKeyGenerator caGenerator = new RootCAKeyGenerator(
                imageBaseFolder.resolve("rootca/newca"),
                keysPath,
                topDomain.getDomainName(),
                topDomain.getOwnerCountry()
        );
        caGenerator.start();
        //TODO the key and cert must be translated back.
        caGenerator.getRootCaDetails();
        for (Interchange interchange : cluster.getInterchanges()) {
            IntermediateDomain domain = interchange.getDomain();
            IntermediateCaCSRGenerator csrGenerator = new IntermediateCaCSRGenerator(
                    imageBaseFolder.resolve("intermediateca/csr"),
                    keysPath,
                    domain.getDomainName(),
                    domain.getOwningCountry()
            );
            csrGenerator.start();
            IntermediateCACertGenerator certGenerator = new IntermediateCACertGenerator(
                    imageBaseFolder.resolve("rootca/sign_intermediate"),
                    csrGenerator.getCsrOnHost(),
                    domain.getDomainName(),
                    caGenerator.getCaCertOnHost(),
                    caGenerator.getCaKeyOnHost(),
                    domain.getOwningCountry(),
                    keysPath
            );
            certGenerator.start();

            for (AdditionalHost host : interchange.getAdditionalHosts()) {
                ServerCertGenerator serverCertGenerator = new ServerCertGenerator(
                        imageBaseFolder.resolve("server"),
                        host.getHostname(),
                        certGenerator.getSingleCertOnHost(),
                        csrGenerator.getKeyOnHost(),
                        certGenerator.getChainCertOnHost(),
                        host.getOwningCountry(),
                        keysPath
                );
                serverCertGenerator.start();
            }
            for (ServicProviderDescription description : interchange.getServiceProviders()) {
                ServiceProviderCSRGenerator spCsr = new ServiceProviderCSRGenerator(
                        imageBaseFolder.resolve("serviceprovider/csr"),
                        keysPath,
                        description.getName(),
                        description.getCountry()
                );
                spCsr.start();
                ServiceProviderCertGenerator spCert = new ServiceProviderCertGenerator(
                        imageBaseFolder.resolve("intermediateca/sign_sp"),
                        spCsr.getCsrOnHost(),
                        description.getName(),
                        certGenerator.getSingleCertOnHost(),
                        certGenerator.getIntermediateKeyOnHost(),
                        certGenerator.getChainCertOnHost(),
                        keysPath
                );
                spCert.start();
            }

        }
    }

    private Cluster createClusterForSystemtest() {
        Cluster cluster = Cluster.builder()
                .topDomain().domainName("bouvetinterchange.eu").ownerCountry("NO").done()
                .interchange().intermediateDomain().domainName("a.bouvetinterchange.eu").ownerCountry("NO").done()
                .serviceProvider().name("king_olav.bouvetinterchange.eu").country("NO").done()
                .done()
                .interchange().intermediateDomain().domainName("b.bouvetinterchange.eu").ownerCountry("NO").done()
                .serviceProvider().name("king_gustaf.bouvetinterchange.eu").country("SE").done()
                .done().done();

        return cluster;
    }

    private Cluster createClusterForSystemtestWithHosts() {
        Cluster cluster = Cluster.builder()
                .topDomain().domainName("bouvetinterchange.eu").ownerCountry("NO").done()
                .interchange().intermediateDomain().domainName("a.bouvetinterchange.eu").ownerCountry("NO").done()
                .serviceProvider().name("king_olav.bouvetinterchange.eu").country("NO").done()
                .done()
                .interchange().intermediateDomain().domainName("b.bouvetinterchange.eu").ownerCountry("NO").done()
                .serviceProvider().name("king_gustaf.bouvetinterchange.eu").country("SE").done()
                .done().done();

        return cluster;
    }
    private Cluster createClusterForSPRedirect() {
        return Cluster.builder()
                .topDomain().domainName("redirect-cluster.eu").ownerCountry("NO").done()
                .interchange()
                    .intermediateDomain().domainName("node-a.no").ownerCountry("NO").done()
                    .serviceProvider().name("service-provider-a").country("NO").done()
                .done()
                .interchange()
                    .intermediateDomain().domainName("node-b.no").ownerCountry("NO").done()
                    .serviceProvider().name("service-provider-b").country("NO").done()
                .done()
                .done();
    }

    private Cluster createClusterModel() {
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
        return myCluster;
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
