package no.vegvesen.ixn.docker.keygen.generator;

import no.vegvesen.ixn.docker.keygen.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public class ClusterKeyGenerator {

    private static final char[] allowedChars = {
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '0','1','2','3','4','5','6','7','8','9',
            '*','-','_','$','+'
    };


    public static void generateKeys(Cluster cluster, Path outputFolder) throws IOException {
        SecureRandom random = new SecureRandom();
        TopDomain topDomain = cluster.getTopDomain();
        CertKeyPair certKeyPair  = generateRootCA(outputFolder, topDomain);
        String topDomainTrustStorePassword = generatePassword(random,24);
        generateTrustore(certKeyPair.getCaCertOnHost(), topDomainTrustStorePassword, outputFolder, topDomain.getDomainName() + "_truststore.jks");
        String passwordFile = topDomain.getDomainName() + "_truststore.txt";
        Files.writeString(outputFolder.resolve(passwordFile),topDomainTrustStorePassword);
        for (IntermediateDomain domain : topDomain.getIntermediateDomains()) {
            IntermediateCaCSRGenerator csrGenerator = generateIntermediateCaCsr(outputFolder, domain.getDomainName(), domain.getOwningCountry());
            IntermediateCACertGenerator certGenerator = generateIntermediateCaCert(outputFolder, csrGenerator.getCsrOnHost(), certKeyPair.getCaCertOnHost(), certKeyPair.getCaKeyOnHost(), domain.getDomainName(), domain.getOwningCountry());
            String intermediateCaKeystorePassword = generatePassword(random,24);
            String keystoreName = domain.getDomainName() + ".p12";
            generateKeystore(outputFolder,
                    intermediateCaKeystorePassword,
                    keystoreName,
                    csrGenerator.getKeyOnHost(),
                    certGenerator.getChainCertOnHost(),
                    certKeyPair.getCaCertOnHost(), domain.getDomainName()
            );
            Files.writeString(outputFolder.resolve(domain.getDomainName() + ".txt"),intermediateCaKeystorePassword);
            Interchange interchange = domain.getInterchange();
            for (AdditionalHost host : interchange.getAdditionalHosts()) {
                generateServerCertForHost(outputFolder, host, certGenerator.getSingleCertOnHost(), certGenerator.getChainCertOnHost(), csrGenerator.getKeyOnHost());
                //TODO create keyStore for host (using chain, I expect)
            }
            for (ServicProviderDescription description : interchange.getServiceProviders()) {
                ServiceProviderCSRGenerator spCsr = generateCsrForServiceProvider(outputFolder, description);
                CertAndCertChain spCert = generateServiceProviderCert(outputFolder, description, certGenerator.getSingleCertOnHost(), certGenerator.getIntermediateKeyOnHost(), certGenerator.getChainCertOnHost(), spCsr.getCsrOnHost());
                String serviceProviderKeystorePassword = generatePassword(random,24);
                String serviceProviderKeystoreName = description.getName() + ".p12";
                Files.writeString(outputFolder.resolve(description.getName() + ".txt"),serviceProviderKeystorePassword);
                generateServiceProviderKeyStore(outputFolder, description, serviceProviderKeystorePassword, serviceProviderKeystoreName, spCsr.getKeyOnHost(), spCert.getCertChainOnHost(), certGenerator.getSingleCertOnHost());
            }

        }
    }

    public static void generateServiceProviderKeyStore(Path outputFolder, ServicProviderDescription description, String serviceProviderKeystorePassword, String serviceProviderKeystoreName, Path keyOnHost, Path certChainOnHost, Path signingCertOnHost) {
        KeystoreGenerator spKeystore = new KeystoreGenerator(
                keyOnHost,
                certChainOnHost,
                description.getName(),
                signingCertOnHost, //TODO check this! This is what I used for the systems test
                serviceProviderKeystorePassword,
                outputFolder.resolve(serviceProviderKeystoreName)
        );
        spKeystore.start();
    }

    public static CertAndCertChain generateServiceProviderCert(Path outputFolder, ServicProviderDescription description, Path singleCertOnHost, Path intermediateKeyOnHost, Path chainCertOnHost, Path csrOnHost) {
        ServiceProviderCertGenerator spCert = new ServiceProviderCertGenerator(
                csrOnHost,
                description.getName(),
                singleCertOnHost,
                intermediateKeyOnHost,
                chainCertOnHost,
                outputFolder
        );
        spCert.start();
        return spCert.getCertAndCertChainOnHost();
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

    public static Path generateKeystore(Path outputFolder, String intermediateCaKeystorePassword, String keystoreName, Path keyOnHost, Path chainCertOnHost, Path caCertOnHost, String domainName) {
        //TODO this should be done inside the KeystoreGenerator class to be consistent.
        Path keystoreOnHost = outputFolder.resolve(keystoreName);
        KeystoreGenerator keystoreGenerator = new KeystoreGenerator(
                keyOnHost,
                chainCertOnHost,
                domainName,
                caCertOnHost,
                intermediateCaKeystorePassword,
                keystoreOnHost
        );
        keystoreGenerator.start();
        return keystoreOnHost;
    }

    public static IntermediateCACertGenerator generateIntermediateCaCert(Path outputFolder, Path csrOnHost, Path caCertOnHost, Path caKeyOnHost, String domainName, String owningCountry) {
        IntermediateCACertGenerator certGenerator = new IntermediateCACertGenerator(
                csrOnHost,
                domainName,
                caCertOnHost,
                caKeyOnHost,
                owningCountry,
                outputFolder
        );
        certGenerator.start();
        return certGenerator;
    }

    public static IntermediateCaCSRGenerator generateIntermediateCaCsr(Path outputFolder, String domainName, String owningCountry) {
        IntermediateCaCSRGenerator csrGenerator = new IntermediateCaCSRGenerator(
                outputFolder,
                domainName,
                owningCountry
        );
        csrGenerator.start();
        return csrGenerator;
    }

    public static void generateTrustore(Path caCertPath, String topDomainTrustStorePassword, Path outputFolder, String truststoreName) {
        TruststoreGenerator topDomainTrustStoreGenerator = new TruststoreGenerator(
                caCertPath,
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

    private static String generatePassword(SecureRandom random, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i =  0; i < length; i++) {
            builder.append(allowedChars[random.nextInt(allowedChars.length)]);
        }
        return builder.toString();
    }

}
