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
    public void foo() {
        //First, describe the cluster, and everything we need.
        Cluster myCluster = createClusterModel();
        //Then, spin through the model, creating keys breadth-first for each level. (So that we might be able to create several keys on the
        //same level using one container call.

        //now, write out what we need to generate for now.
        TopDomain topDomain = myCluster.getTopDomain();
        System.out.println(String.format("Generate CA key: %s, %s", topDomain.getDomainName(),topDomain.getOwnerCountry()));
        for (Interchange interchange : myCluster.getInterchanges()) {
            IntermediateDomain domain = interchange.getDomain();
            System.out.println(String.format("\tGenerate intermediate CA: %s, %s", domain.getDomainName(),domain.getOwningCountry()));
            for (AdditionalHost additionalHost : interchange.getAdditionalHosts()) {
                System.out.println(String.format("\t\tGenerate additional host %s , %s",additionalHost.getHostname(),additionalHost.getOwningCountry()));
            }
            for (ServicProviderDescription sp : interchange.getServiceProviders()) {
                System.out.println(String.format("\t\tGenerate Service provider %s, %s", sp.getName(),sp.getCountry()));
            }

        }
    }

    @Test
    public void generate() {
        Cluster cluster = createClusterModel();
        Path imageBaseFolder = DockerBaseIT.getFolderPath("keymaster");
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
                        spCsr.getKeyOnHost(),
                        certGenerator.getChainCertOnHost(),
                        keysPath
                );
                spCert.start();



            }

        }

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


}
