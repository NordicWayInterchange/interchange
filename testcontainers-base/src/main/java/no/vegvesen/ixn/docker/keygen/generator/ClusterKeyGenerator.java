package no.vegvesen.ixn.docker.keygen.generator;

import no.vegvesen.ixn.docker.keygen.*;

import java.nio.file.Path;

public class ClusterKeyGenerator {
    public static void generateKeys(Cluster cluster, Path imageBaseFolder, Path outputFolder) {

        TopDomain topDomain = cluster.getTopDomain();
        RootCAKeyGenerator caGenerator = new RootCAKeyGenerator(
                imageBaseFolder.resolve("rootca/newca"),
                outputFolder,
                topDomain.getDomainName(),
                topDomain.getOwnerCountry()
        );
        caGenerator.start();
        //TODO the key and cert must be translated back.
        TruststoreGenerator topDomainTrustStoreGenerator = new TruststoreGenerator(
                imageBaseFolder.resolve("truststores"),
                caGenerator.getCaCertOnHost(),
                "password", //For now
                outputFolder.resolve(topDomain.getDomainName() + "_truststore.jks")
        );
        topDomainTrustStoreGenerator.start();
        for (Interchange interchange : cluster.getInterchanges()) {
            IntermediateDomain domain = interchange.getDomain();
            IntermediateCaCSRGenerator csrGenerator = new IntermediateCaCSRGenerator(
                    imageBaseFolder.resolve("intermediateca/csr"),
                    outputFolder,
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
                    outputFolder
            );
            certGenerator.start();
            KeystoreGenerator keystoreGenerator = new KeystoreGenerator(
                    imageBaseFolder.resolve("keystores"),
                    csrGenerator.getKeyOnHost(),
                    certGenerator.getChainCertOnHost(),
                    domain.getDomainName(),
                    caGenerator.getCaCertOnHost(),
                    "password",
                    outputFolder.resolve(domain.getDomainName() + ".p12")
            );
            keystoreGenerator.start();
            TruststoreGenerator intermediateTruststore = new TruststoreGenerator(
                    imageBaseFolder.resolve("truststores"),
                    certGenerator.getSingleCertOnHost(),
                    "password",
                    outputFolder.resolve(domain.getDomainName() + "_truststore.jks")
            );
            intermediateTruststore.start();
            for (AdditionalHost host : interchange.getAdditionalHosts()) {
                ServerCertGenerator serverCertGenerator = new ServerCertGenerator(
                        imageBaseFolder.resolve("server"),
                        host.getHostname(),
                        certGenerator.getSingleCertOnHost(),
                        csrGenerator.getKeyOnHost(),
                        certGenerator.getChainCertOnHost(),
                        host.getOwningCountry(),
                        outputFolder
                );
                serverCertGenerator.start();
                //TODO create keyStore for host (using chain, I expect)
            }
            for (ServicProviderDescription description : interchange.getServiceProviders()) {
                ServiceProviderCSRGenerator spCsr = new ServiceProviderCSRGenerator(
                        imageBaseFolder.resolve("serviceprovider/csr"),
                        outputFolder,
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
                        outputFolder
                );
                spCert.start();
                KeystoreGenerator spKeystore = new KeystoreGenerator(
                        imageBaseFolder.resolve("keystores"),
                        spCsr.getKeyOnHost(),
                        spCert.getCertChainOnHost(),
                        description.getName(),
                        certGenerator.getSingleCertOnHost(), //TODO check this! This is what I used for the systems test
                        "password",
                        outputFolder.resolve(description.getName() + ".p12")
                );
                spKeystore.start();

            }

        }
    }

}
