package no.vegvesen.ixn.docker.keygen.generator;

import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.docker.keygen.*;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemWriter;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class ClusterKeyGenerator {

    private static final char[] allowedChars = {
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '0','1','2','3','4','5','6','7','8','9',
            '*','-','_','$','+'
    };


    public static void generateKeys(Cluster cluster, Path outputFolder) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, OperatorCreationException {
        SecureRandom random = new SecureRandom();
        TopDomain topDomain = cluster.getTopDomain();
        CertKeyPair certKeyPair  = generateRootCABC(outputFolder, topDomain);
        String topDomainTrustStorePassword = generatePassword(random,24);
        String caCertAlias = "myKey";
        Path truststorePath = generateTrustStoreBC(certKeyPair.getCaCertOnHost(),topDomainTrustStorePassword,outputFolder,topDomain.getDomainName() + "_truststore.jks", caCertAlias);
        String passwordFile = topDomain.getDomainName() + "_truststore.txt";
        Files.writeString(outputFolder.resolve(passwordFile),topDomainTrustStorePassword);
        for (IntermediateDomain domain : topDomain.getIntermediateDomains()) {
            IntermediateCaCSRGenerator csrGenerator = generateIntermediateCaCsr(outputFolder, domain.getDomainName(), domain.getOwningCountry());
            IntermediateCACertGenerator certGenerator = generateIntermediateCaCert(outputFolder, csrGenerator.getCsrOnHost(), certKeyPair.getCaCertOnHost(), certKeyPair.getCaKeyOnHost(), domain.getDomainName(), domain.getOwningCountry());
            String intermediateCaKeystorePassword = generatePassword(random,24);
            String keystoreName = domain.getDomainName() + ".p12";
            Path intermediateKeystoreName = generateKeystore(outputFolder,
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
                CsrKeyPair csrKeyPair = generateCsrForServiceProvider(outputFolder, description);
                CertAndCertChain spCert = generateServiceProviderCertBC(outputFolder, description.getName(), intermediateCaKeystorePassword,domain.getDomainName(), csrKeyPair.getCsrOnHost(), loadStore(intermediateKeystoreName, intermediateCaKeystorePassword, "PKCS12"), loadStore(truststorePath, topDomainTrustStorePassword, "JKS"), caCertAlias);
                String serviceProviderKeystorePassword = generatePassword(random,24);
                String serviceProviderKeystoreName = description.getName() + ".p12";
                Files.writeString(outputFolder.resolve(description.getName() + ".txt"),serviceProviderKeystorePassword);
                generateServiceProviderKeyStore(outputFolder, description, serviceProviderKeystorePassword, serviceProviderKeystoreName, csrKeyPair.getKeyOnHost(), spCert.getCertChainOnHost(), certGenerator.getSingleCertOnHost());
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

    public static CertAndCertChain generateServiceProviderCertBC(Path outputPath, String spName, String intermediateCaKeystorePassword, String keyAlias, Path csr, KeyStore keyStore, KeyStore truststore, String caCertAlias) {
        try {
            CertSigner certSigner = new CertSigner(keyStore,keyAlias,intermediateCaKeystorePassword, truststore, caCertAlias);
            String csrAsString = Files.readString(csr);
            List<String> certChain = certSigner.sign(csrAsString, spName);
            Path certPath = Files.writeString(outputPath.resolve(spName + ".crt.pem"),certChain.get(0));
            Path certPathChain = Files.writeString(outputPath.resolve("chain." + spName + ".crt.pem"),String.join("",certChain));
            return new CertAndCertChain(certPath,certPathChain);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                 UnrecoverableKeyException | OperatorCreationException e) {
            throw new RuntimeException(e);
        }

    }

    @NotNull
    private static KeyStore loadStore(Path keystorePath, String keystorePassword, String keystoreType) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance(keystoreType);
        keyStore.load(new FileInputStream(keystorePath.toFile()), keystorePassword.toCharArray());
        return keyStore;
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


    public static CsrKeyPair generateCsrForServiceProvider(Path outputFolder, ServicProviderDescription description) {
        ServiceProviderCSRGenerator spCsr = new ServiceProviderCSRGenerator(
                outputFolder,
                description.getName(),
                description.getCountry()
        );
        spCsr.start();
        return spCsr.getCsrKeyPairOnHost();
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


    public static Path generateTrustStoreBC(Path caCertPath, String topDomainTruststorePassword, Path outputFolder, String truststoreName, String caCertAlias) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        PEMParser pemParser = new PEMParser(new FileReader(caCertPath.toFile()));
        X509CertificateHolder certificateHolder = (X509CertificateHolder)pemParser.readObject();
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        X509Certificate certificate = converter.getCertificate(certificateHolder);

        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null,null);
        trustStore.setCertificateEntry(caCertAlias,certificate);
        Path storeLocation = outputFolder.resolve(truststoreName);
        trustStore.store(new FileOutputStream(storeLocation.toFile()),topDomainTruststorePassword.toCharArray());
        return storeLocation;
    }

    public static Path generateTrustore(Path caCertPath, String topDomainTrustStorePassword, Path outputFolder, String truststoreName) {
        Path generatedTrustStore = outputFolder.resolve(truststoreName);
        TruststoreGenerator topDomainTrustStoreGenerator = new TruststoreGenerator(
                caCertPath,
                topDomainTrustStorePassword, //For now
                generatedTrustStore
        );
        topDomainTrustStoreGenerator.start();
        return generatedTrustStore;
    }

    public static CertKeyPair generateRootCABC(Path outputFolder, TopDomain topDomain) throws NoSuchAlgorithmException, OperatorCreationException, IOException, CertificateException {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        X500Principal x500Principal = new X500Principal(
                String.format(
                        "CN=%s, O=Nordic Way, C=%s",
                        topDomain.getDomainName(),
                        topDomain.getOwnerCountry()
                )
        );
        JcaContentSignerBuilder signBuilder = new JcaContentSignerBuilder("SHA512withRSA");
        BigInteger serial = new BigInteger(Long.toString(secureRandom.nextLong()));
        Date fromDate = new Date();
        Date toDate = Date.from(LocalDateTime.now().plus(1, ChronoUnit.YEARS).atZone(ZoneId.systemDefault()).toInstant());
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                x500Principal,
                serial,
                fromDate,
                toDate,
                x500Principal,
                keyPair.getPublic()
        );
        certBuilder.addExtension(Extension.basicConstraints,true,new BasicConstraints(true));
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign);
        certBuilder.addExtension(Extension.keyUsage,true,keyUsage);
        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
        AuthorityKeyIdentifier authorityKeyIdentifier = extensionUtils.createAuthorityKeyIdentifier(keyPair.getPublic());
        certBuilder.addExtension(Extension.authorityKeyIdentifier,false,authorityKeyIdentifier);
        SubjectKeyIdentifier subjectKeyIdentifier = extensionUtils.createSubjectKeyIdentifier(keyPair.getPublic());
        certBuilder.addExtension(Extension.subjectKeyIdentifier,false,subjectKeyIdentifier);
        ContentSigner signer = signBuilder.build(keyPair.getPrivate());
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));
        Path keyPath = outputFolder.resolve(String.format("ca.%s.key.pem", topDomain.getDomainName()));
        FileWriter keyOut = new FileWriter(keyPath.toFile());
        JcaPEMWriter keyWriter = new JcaPEMWriter(keyOut);
        keyWriter.writeObject(keyPair);
        keyWriter.close();
        keyOut.close();
        Path certPath = outputFolder.resolve(String.format("ca.%s.crt.pem", topDomain.getDomainName()));
        FileWriter certOut = new FileWriter(certPath.toFile());
        JcaPEMWriter certWriter = new JcaPEMWriter(certOut);
        certWriter.writeObject(cert);
        certWriter.close();
        certOut.close();
        return new CertKeyPair(certPath,keyPath);



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
