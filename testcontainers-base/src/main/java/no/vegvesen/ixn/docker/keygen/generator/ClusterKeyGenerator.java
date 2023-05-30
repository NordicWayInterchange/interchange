package no.vegvesen.ixn.docker.keygen.generator;

import no.vegvesen.ixn.docker.keygen.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class ClusterKeyGenerator {
    public static void generateKeys(Cluster cluster, Path outputFolder) {

        TopDomain topDomain = cluster.getTopDomain();
        CertKeyPair certKeyPair  = generateRootCA(outputFolder, topDomain);
        String topDomainTrustStorePassword = "password";
        generateTrustore(certKeyPair.getCaCertOnHost(), topDomainTrustStorePassword, outputFolder, topDomain.getDomainName() + "_truststore.jks");
        for (Interchange interchange : cluster.getInterchanges()) {
            IntermediateDomain domain = interchange.getDomain();
            IntermediateCaCSRGenerator csrGenerator = generateIntermediateCaCsr(outputFolder, domain);
            IntermediateCACertGenerator certGenerator = generateIntermediateCaCert(outputFolder, domain, csrGenerator.getCsrOnHost(), certKeyPair.getCaCertOnHost(), certKeyPair.getCaKeyOnHost());
            String intermediateCaKeystorePassword = "password";
            String keystoreName = domain.getDomainName() + ".p12";
            generateKeystore(outputFolder, domain, intermediateCaKeystorePassword, keystoreName, csrGenerator.getKeyOnHost(), certGenerator.getChainCertOnHost(), certKeyPair.getCaCertOnHost());
            generateTrustore(certGenerator.getSingleCertOnHost(), "password", outputFolder, domain.getDomainName() + "_truststore.jks");
            for (AdditionalHost host : interchange.getAdditionalHosts()) {
                generateServerCertForHost(outputFolder, host, certGenerator.getSingleCertOnHost(), certGenerator.getChainCertOnHost(), csrGenerator.getKeyOnHost());
                //TODO create keyStore for host (using chain, I expect)
            }
            for (ServicProviderDescription description : interchange.getServiceProviders()) {
                ServiceProviderCSRGenerator spCsr = generateCsrForServiceProvider(outputFolder, description);
                ServiceProviderCertGenerator spCert = generateServiceProviderCert(outputFolder, description, certGenerator.getSingleCertOnHost(), certGenerator.getIntermediateKeyOnHost(), certGenerator.getChainCertOnHost(), spCsr.getCsrOnHost());
                String serviceProviderKeystorePassword = "password";
                String serviceProviderKeystoreName = description.getName() + ".p12";
                generateServiceProviderKeyStore(outputFolder, description, serviceProviderKeystorePassword, serviceProviderKeystoreName, spCsr.getKeyOnHost(), spCert.getCertChainOnHost(), certGenerator.getSingleCertOnHost());

            }

        }
    }

    private static void generateServiceProviderKeyStore(Path outputFolder, ServicProviderDescription description, String serviceProviderKeystorePassword, String serviceProviderKeystoreName, Path keyOnHost, Path certChainOnHost, Path singleCertOnHost) {
        KeystoreGenerator spKeystore = new KeystoreGenerator(
                keyOnHost,
                certChainOnHost,
                description.getName(),
                singleCertOnHost, //TODO check this! This is what I used for the systems test
                serviceProviderKeystorePassword,
                outputFolder.resolve(serviceProviderKeystoreName)
        );
        spKeystore.start();
    }

    @NotNull
    private static ServiceProviderCertGenerator generateServiceProviderCert(Path outputFolder, ServicProviderDescription description, Path singleCertOnHost, Path intermediateKeyOnHost, Path chainCertOnHost, Path csrOnHost) {
        ServiceProviderCertGenerator spCert = new ServiceProviderCertGenerator(
                csrOnHost,
                description.getName(),
                singleCertOnHost,
                intermediateKeyOnHost,
                chainCertOnHost,
                outputFolder
        );
        spCert.start();
        return spCert;
    }

    public static ServiceProviderCSRGenerator generateCsrForServiceProvider(Path outputFolder, ServicProviderDescription description) {
        ServiceProviderCSRGenerator spCsr = new ServiceProviderCSRGenerator(
                outputFolder,
                description.getName(),
                description.getCountry()
        );
        spCsr.start();
        return spCsr;
    }

    private static void generateServerCertForHost(Path outputFolder, AdditionalHost host, Path singleCertOnHost, Path chainCertOnHost, Path keyOnHost) {
        ServerCertGenerator serverCertGenerator = new ServerCertGenerator(
                host.getHostname(),
                singleCertOnHost,
                keyOnHost,
                chainCertOnHost,
                host.getOwningCountry(),
                outputFolder
        );
        serverCertGenerator.start();
    }

    private static void generateKeystore(Path outputFolder, IntermediateDomain domain, String intermediateCaKeystorePassword, String keystoreName, Path keyOnHost, Path chainCertOnHost, Path caCertOnHost) {
        KeystoreGenerator keystoreGenerator = new KeystoreGenerator(
                keyOnHost,
                chainCertOnHost,
                domain.getDomainName(),
                caCertOnHost,
                intermediateCaKeystorePassword,
                outputFolder.resolve(keystoreName)
        );
        keystoreGenerator.start();
    }

    public static IntermediateCACertGenerator generateIntermediateCaCert(Path outputFolder, IntermediateDomain domain, Path csrOnHost, Path caCertOnHost, Path caKeyOnHost) {
        IntermediateCACertGenerator certGenerator = new IntermediateCACertGenerator(
                csrOnHost,
                domain.getDomainName(),
                caCertOnHost,
                caKeyOnHost,
                domain.getOwningCountry(),
                outputFolder
        );
        certGenerator.start();
        return certGenerator;
    }

    public static IntermediateCaCSRGenerator generateIntermediateCaCsr(Path outputFolder, IntermediateDomain domain) {
        IntermediateCaCSRGenerator csrGenerator = new IntermediateCaCSRGenerator(
                outputFolder,
                domain.getDomainName(),
                domain.getOwningCountry()
        );
        csrGenerator.start();
        return csrGenerator;
    }

    public static void generateTrustore(Path caGenerator, String topDomainTrustStorePassword, Path outputFolder, String truststoreName) {
        TruststoreGenerator topDomainTrustStoreGenerator = new TruststoreGenerator(
                caGenerator,
                topDomainTrustStorePassword, //For now
                outputFolder.resolve(truststoreName)
        );
        topDomainTrustStoreGenerator.start();
    }

    public static CertKeyPair generateRootCA(Path outputFolder, TopDomain topDomain) {
        RootCAKeyGenerator caGenerator = new RootCAKeyGenerator(
                outputFolder,
                topDomain.getDomainName(),
                topDomain.getOwnerCountry()
        );
        caGenerator.start();
        return caGenerator.getCertKeyPairOnHost();
    }

}
