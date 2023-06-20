package no.vegvesen.ixn.docker.keygen.generator;

import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.docker.keygen.*;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX500NameUtil;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ClusterKeyGenerator {

    private static final char[] allowedChars = {
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '0','1','2','3','4','5','6','7','8','9',
            '*','-','_','$','+'
    };


    public static void generateKeys(Cluster cluster, Path outputFolder) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, OperatorCreationException, NoSuchProviderException, SignatureException, InvalidKeyException {
        //Provider bc = Security.getProvider("BC");
        //if (bc == null) {
        //    Security.addProvider(new BouncyCastleProvider());
        //}
        Files.createDirectories(outputFolder);
        SecureRandom random = new SecureRandom();
        TopDomain topDomain = cluster.getTopDomain();
        KeyPairAndCertificate topCa = generateTopCa(topDomain);
        saveKeyPair(outputFolder,String.format("ca.%s.key.pem",topDomain.getDomainName()),topCa.getKeyPair());
        saveCert(outputFolder,String.format("ca.%s.crt.pem",topDomain.getDomainName()),topCa.getCertificate());
        String topDomainTrustStorePassword = generatePassword(random,24);
        String caCertAlias = "myKey";
        makeTrustStore(topDomainTrustStorePassword,outputFolder,topDomain.getDomainName() + "_truststore.jks", caCertAlias,topCa.getCertificate());

        String passwordFile = topDomain.getDomainName() + "_truststore.txt";
        Files.writeString(outputFolder.resolve(passwordFile),topDomainTrustStorePassword);
        for (IntermediateDomain domain : topDomain.getIntermediateDomains()) {
            KeyPairAndCsr intermediateCsr = generateIntermediateKeypairAndCsr(domain.getDomainName(), domain.getOwningCountry());
            CertificateAndCertificateChain intermediateCert = signIntermediateCsr(topCa.getCertificate(), topCa.getKeyPair(), intermediateCsr.getCsr());
            String intermediateCaKeystorePassword = generatePassword(random,24);
            String keystoreName = domain.getDomainName() + ".p12";
            generateKeystoreBC(outputFolder,intermediateCaKeystorePassword,keystoreName, domain.getDomainName(), intermediateCsr.getKeyPair().getPrivate(), intermediateCert.getChain().toArray(new X509Certificate[0]));
            Files.writeString(outputFolder.resolve(domain.getDomainName() + ".txt"),intermediateCaKeystorePassword);
            Interchange interchange = domain.getInterchange();
            for (AdditionalHost host : interchange.getAdditionalHosts()) {
                CertificateCertificateChainAndKeys additionalhostKeys = generateServerCertForHost(host, intermediateCert.getCertificate(), intermediateCert.getChain(), intermediateCsr.getKeyPair().getPrivate());
                saveCert(outputFolder, String.format("%s.crt.pem", host.getHostname()), additionalhostKeys.getCertificate());
                saveCertChain(outputFolder, String.format("chain.%s.crt.pem", host.getHostname()), additionalhostKeys.getCertificateChain());
                saveKeyPair(outputFolder, String.format("int.%s.key.pem", host.getHostname()),additionalhostKeys.getKeyPair());
                //generateServerCertForHost(outputFolder, host, intermediateCertOnHost, intermediateCertChainOnHost, intermediateKeyPath);
                //TODO create keyStore for host (using chain, I expect)
            }
            for (ServicProviderDescription description : interchange.getServiceProviders()) {
                KeyPairAndCsr spCsr = generateCsrForServiceProviderBC(description);
                List<X509Certificate> spCertChain = generateServiceProviderCertBC(spCsr.getCsr(), topCa.getCertificate(), intermediateCert.getCertificate(), intermediateCsr.getKeyPair().getPrivate());
                String serviceProviderKeystorePassword = generatePassword(random,24);
                String serviceProviderKeystoreName = description.getName() + ".p12";
                Files.writeString(outputFolder.resolve(description.getName() + ".txt"),serviceProviderKeystorePassword);
                X509Certificate[] certChain = spCertChain.toArray(new X509Certificate[0]);
                generateKeystoreBC(outputFolder,serviceProviderKeystorePassword,serviceProviderKeystoreName, description.getName(), spCsr.getKeyPair().getPrivate(), certChain);
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

    public static List<X509Certificate> generateServiceProviderCertBC(PKCS10CertificationRequest certificationRequest, X509Certificate caCertificate, X509Certificate intermediateCertificate, PrivateKey privateKey) {
        try {
            CertSigner certSigner = new CertSigner(privateKey, intermediateCertificate, caCertificate);
            List<X509Certificate> certChain = certSigner.sign(certificationRequest);
            return certChain;
        } catch (IOException | NoSuchAlgorithmException | CertificateException |
                 OperatorCreationException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }

    }

    public static KeyStore loadStore(Path keystorePath, String keystorePassword, String keystoreType) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
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

    public static KeyPairAndCsr generateCsrForServiceProviderBC(ServicProviderDescription description) throws NoSuchAlgorithmException, OperatorCreationException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        X500Principal x500Principal = new X500Principal(
                String.format(
                        "emailAddress=test@test.com, CN=%s, O=Nordic Way, C=%s",
                        description.getName(),
                        description.getCountry()
                )
        );
        PKCS10CertificationRequest csr = createCertificateRequest(x500Principal, keyPair, "SHA256withRSA");
        return new KeyPairAndCsr(keyPair,csr);
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

    
    private static CertificateCertificateChainAndKeys generateServerCertForHost(AdditionalHost host,  X509Certificate intermediateCertificate,List<X509Certificate> intermediateCertificateChain, PrivateKey intermediateKey) throws NoSuchAlgorithmException, OperatorCreationException, CertIOException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException {
        KeyPair keyPair = generateKeyPair(2048);
        X500Principal subject = new X500Principal(String.format(
                "O=Nordic Way, CN=%s",
                host.getHostname()
        ));
        PKCS10CertificationRequest csr = createCertificateRequest(subject, keyPair, "SHA512withRSA");
        SubjectPublicKeyInfo csrSubjectPublicKeyInfo = csr.getSubjectPublicKeyInfo();
        BigInteger serialNumber = new BigInteger(Long.toString(new SecureRandom().nextLong()));
        Date startDate = new Date();
        Date toDate = Date.from(LocalDateTime.now().plus(1, ChronoUnit.YEARS).atZone(ZoneId.systemDefault()).toInstant());
        X500Name issuerSubject = JcaX500NameUtil.getSubject(intermediateCertificate);
        X500Name csrSubject = csr.getSubject();
        JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                issuerSubject,
                serialNumber,
                startDate,
                toDate,
                csrSubject,
                csrSubjectPublicKeyInfo
        );
        certificateBuilder.addExtension(Extension.basicConstraints,false,new BasicConstraints(false));
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation | KeyUsage.keyEncipherment);
        certificateBuilder.addExtension(Extension.keyUsage,true,keyUsage);
        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
        AuthorityKeyIdentifier authorityKeyIdentifier = extensionUtils.createAuthorityKeyIdentifier(intermediateCertificate.getPublicKey());
        certificateBuilder.addExtension(Extension.authorityKeyIdentifier,false, authorityKeyIdentifier);
        SubjectKeyIdentifier subjectKeyIdentifier = extensionUtils.createSubjectKeyIdentifier(csrSubjectPublicKeyInfo);
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier,false, subjectKeyIdentifier);
        KeyPurposeId[] keyPurposeIds = new KeyPurposeId[] {KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth};
        ExtendedKeyUsage extendedKeyUsage = new ExtendedKeyUsage(keyPurposeIds);
        certificateBuilder.addExtension(Extension.extendedKeyUsage,false,extendedKeyUsage);
        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA512withRSA");

        ContentSigner signer = signerBuilder.build(intermediateKey);
        X509CertificateHolder issuedCertHolder = certificateBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(issuedCertHolder);
        //TODO do we need BC here?
        certificate.verify(intermediateCertificate.getPublicKey());
        List<X509Certificate> certificateChain = new ArrayList<>(intermediateCertificateChain);
        certificateChain.add(0,certificate);
        return new CertificateCertificateChainAndKeys(keyPair,certificate,certificateChain);

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


    public static Path generateKeystoreBC(Path outputFolder, String keystorePassword, String keystoreName, String entryName, PrivateKey privateKey, Certificate[] certificates) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null,null);
        keyStore.setKeyEntry(entryName, privateKey,null, certificates);

        Path outputPath = outputFolder.resolve(keystoreName);
        keyStore.store(new FileOutputStream(outputPath.toFile()),keystorePassword.toCharArray());
        return outputPath;
    }

    @NotNull
    public static Certificate[] getCertChain(List<String> certChain) throws IOException, CertificateException {
        Certificate[] certificates = new Certificate[certChain.size()];
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        for (int i = 0; i < certChain.size(); i++) {
            PEMParser pemParser = new PEMParser(new StringReader(certChain.get(i)));
            X509CertificateHolder certificate = (X509CertificateHolder) pemParser.readObject();
            certificates[i] = converter.getCertificate(certificate);
        }
        return certificates;
    }

    private static PrivateKey getPrivateKey(Path keyOnHost) throws IOException {
        PEMParser pemParser = new PEMParser(new FileReader(keyOnHost.toFile()));
        Object pemOut = pemParser.readObject();
        PrivateKey privateKey;
        if (pemOut instanceof  PrivateKeyInfo) {
            privateKey = new JcaPEMKeyConverter().getPrivateKey((PrivateKeyInfo) pemOut);

        } else if (pemOut instanceof  PEMKeyPair) {
            privateKey = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) pemOut).getPrivate();
        } else {
            throw new RuntimeException(String.format("Cannot determine the type of private key for file %s", keyOnHost));
        }
        return privateKey;
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

    public static CertAndCertChain generateIntermediateCaCertBC(Path outputFolder, String domainName, String owningCountry, X509Certificate caCert, KeyPair caKeyPair, PKCS10CertificationRequest csr) throws IOException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException, OperatorCreationException {

        CertificateAndCertificateChain result = signIntermediateCsr(caCert, caKeyPair, csr);


        String fileName = String.format("int.%s.crt.pem", domainName);
        X509Certificate certificate = result.getCertificate();
        Path singleCert = saveCert(outputFolder, fileName, certificate);
        JcaPEMWriter pemWriter;
        String chainFileName = String.format("chain.%s.crt.pem", domainName);
        Path chain = saveCertChain(outputFolder, chainFileName, result.getChain());
        StringWriter singleCertAsString = new StringWriter();
        pemWriter = new JcaPEMWriter(singleCertAsString);
        pemWriter.writeObject(result.getCertificate());
        pemWriter.close();
        StringWriter caCertAsString = new StringWriter();
        pemWriter = new JcaPEMWriter(caCertAsString);
        pemWriter.writeObject(result.getChain().get(1));
        pemWriter.close();
        return new CertAndCertChain(singleCert,chain,Arrays.asList(singleCertAsString.toString(),caCertAsString.toString()));



    }

    private static Path saveCertChain(Path outputFolder, String chainFileName, List<X509Certificate> certificateChain) throws IOException {
        Path chain = outputFolder.resolve(chainFileName);
        JcaPEMWriter pemWriter;
        pemWriter = new JcaPEMWriter(new FileWriter(chain.toFile()));
        for (X509Certificate cert : certificateChain) {
            pemWriter.writeObject(cert);
        }
        pemWriter.close();
        return chain;
    }

    private static Path saveCert(Path outputFolder, String fileName, X509Certificate certificate) throws IOException {
        Path singleCert = outputFolder.resolve(fileName);
        JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(singleCert.toFile()));
        pemWriter.writeObject(certificate);
        pemWriter.close();
        return singleCert;
    }

    private static CertificateAndCertificateChain signIntermediateCsr(X509Certificate caCert, KeyPair caKeyPair, PKCS10CertificationRequest csr) throws CertIOException, NoSuchAlgorithmException, OperatorCreationException, CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {
        SubjectPublicKeyInfo csrSubjectPublicKeyInfo = csr.getSubjectPublicKeyInfo();
        BigInteger serialNumber = new BigInteger(Long.toString(new SecureRandom().nextLong()));
        Date startDate = new Date();
        Date toDate = Date.from(LocalDateTime.now().plus(1, ChronoUnit.YEARS).atZone(ZoneId.systemDefault()).toInstant());

        X500Name issuerSubject = JcaX500NameUtil.getSubject(caCert);
        X500Name csrSubject = csr.getSubject();
        JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                issuerSubject,
                serialNumber,
                startDate,
                toDate,
                csrSubject,
                csrSubjectPublicKeyInfo
        );
        certificateBuilder.addExtension(Extension.basicConstraints,true,new BasicConstraints(0));
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign);
        certificateBuilder.addExtension(Extension.keyUsage,true,keyUsage);
        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
        AuthorityKeyIdentifier authorityKeyIdentifier = extensionUtils.createAuthorityKeyIdentifier(caCert.getPublicKey());
        certificateBuilder.addExtension(Extension.authorityKeyIdentifier,false, authorityKeyIdentifier);
        SubjectKeyIdentifier subjectKeyIdentifier = extensionUtils.createSubjectKeyIdentifier(csrSubjectPublicKeyInfo);
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier,false, subjectKeyIdentifier);
        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA512withRSA");

        ContentSigner signer = signerBuilder.build(caKeyPair.getPrivate());


        X509CertificateHolder issuedCertHolder = certificateBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(issuedCertHolder);
        //TODO do we need BC here?
        certificate.verify(caCert.getPublicKey());

        CertificateAndCertificateChain result = new CertificateAndCertificateChain(certificate, Arrays.asList(certificate, caCert));
        return result;
    }

    private static X509Certificate getCertificate(Path caCertOnHost) throws IOException, CertificateException {
        PEMParser pemParser;
        pemParser = new PEMParser(new FileReader(caCertOnHost.toFile()));
        X509CertificateHolder certificateHolder = (X509CertificateHolder)pemParser.readObject();
        pemParser.close();
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        X509Certificate caCert = converter.getCertificate(certificateHolder);
        return caCert;
    }

    private static KeyPair getKeyPair(Path caKeyOnHost) throws IOException {
        PEMParser pemParser;
        pemParser = new PEMParser(new FileReader(caKeyOnHost.toFile()));
        JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter();
        KeyPair keyPair = keyConverter.getKeyPair((PEMKeyPair) pemParser.readObject());
        return keyPair;
    }

    private static PKCS10CertificationRequest getPkcs10CertificationRequest(Path csrOnHost) throws IOException {
        PEMParser pemParser = new PEMParser(new FileReader(csrOnHost.toFile()));
        PKCS10CertificationRequest csr = (PKCS10CertificationRequest) pemParser.readObject();
        pemParser.close();
        return csr;
    }

    public static CertChainAndKey generateIntermediateCaCert(Path outputFolder, Path csrOnHost, Path caCertOnHost, Path caKeyOnHost, String domainName, String owningCountry) {
        IntermediateCACertGenerator certGenerator = new IntermediateCACertGenerator(
                csrOnHost,
                domainName,
                caCertOnHost,
                caKeyOnHost,
                owningCountry,
                outputFolder
        );
        certGenerator.start();
        return certGenerator.getCertChainAndKeyOnHost();
    }



    public static CsrKeyPair generateIntermediateCaCsrBC(Path outputFolder, String domainName, String owningCountry) throws OperatorCreationException, NoSuchAlgorithmException, IOException {
        KeyPairAndCsr intermediateCsr = generateIntermediateKeypairAndCsr(domainName, owningCountry);
        Path csrPath = outputFolder.resolve(String.format("int.%s.csr", domainName));
        FileWriter csrWriter = new FileWriter(csrPath.toFile());
        JcaPEMWriter pemWriter = new JcaPEMWriter(csrWriter);
        pemWriter.writeObject(intermediateCsr.getCsr());
        pemWriter.close();
        String fileName = String.format("int.%s.key.pem", domainName);
        KeyPair keyPair = intermediateCsr.getKeyPair();
        Path keyPath = saveKeyPair(outputFolder, fileName, keyPair);
        return new CsrKeyPair(csrPath,keyPath);
    }

    @NotNull
    private static Path saveKeyPair(Path outputFolder, String fileName, KeyPair keyPair) throws IOException {
        JcaPEMWriter pemWriter;
        Path keyPath = outputFolder.resolve(fileName);
        FileWriter keyWriter = new FileWriter(keyPath.toFile());
        pemWriter = new JcaPEMWriter(keyWriter);
        pemWriter.writeObject(keyPair);
        pemWriter.close();
        return keyPath;
    }

    @NotNull
    private static KeyPairAndCsr generateIntermediateKeypairAndCsr(String domainName, String owningCountry) throws NoSuchAlgorithmException, OperatorCreationException {
        KeyPair keyPair = generateKeyPair(4096);
        PKCS10CertificationRequest csr = createCsr(domainName, owningCountry, keyPair);
        KeyPairAndCsr intermediateCsr = new KeyPairAndCsr(keyPair,csr);
        return intermediateCsr;
    }

    private static PKCS10CertificationRequest createCsr(String domainName, String owningCountry, KeyPair keyPair) throws OperatorCreationException {
        X500Principal x500Principal = new X500Principal(
                String.format(
                        "CN=%s, O=Nordic Way, C=%s",
                        domainName,
                        owningCountry
                )
        );
        PrivateKey privateKey = keyPair.getPrivate();
        PKCS10CertificationRequest csr = createCertificateRequest(x500Principal, keyPair, "SHA512withRSA");
        return csr;
    }

    private static PKCS10CertificationRequest createCertificateRequest(X500Principal subject, KeyPair keyPair, String signAlgorithm) throws OperatorCreationException {
        JcaPKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
        JcaContentSignerBuilder signBuilder = new JcaContentSignerBuilder(signAlgorithm);
        ContentSigner signer = signBuilder.build(keyPair.getPrivate());
        PKCS10CertificationRequest csr = builder.build(signer);
        return csr;
    }

    public static CsrKeyPair generateIntermediateCaCsr(Path outputFolder, String domainName, String owningCountry) {
        IntermediateCaCSRGenerator csrGenerator = new IntermediateCaCSRGenerator(
                outputFolder,
                domainName,
                owningCountry
        );
        csrGenerator.start();
        return csrGenerator.getCsrKeyPairOnHost();
    }


    public static Path generateTrustStoreBC(Path caCertPath, String topDomainTruststorePassword, Path outputFolder, String truststoreName, String caCertAlias) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        X509Certificate certificate = loadCertificateFromFile(caCertPath);

        Path storeLocation = makeTrustStore(topDomainTruststorePassword, outputFolder, truststoreName, caCertAlias, certificate);
        return storeLocation;
    }

    private static Path makeTrustStore(String topDomainTruststorePassword, Path outputFolder, String truststoreName, String caCertAlias, X509Certificate certificate) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null,null);
        trustStore.setCertificateEntry(caCertAlias, certificate);
        Path storeLocation = outputFolder.resolve(truststoreName);
        trustStore.store(new FileOutputStream(storeLocation.toFile()), topDomainTruststorePassword.toCharArray());
        return storeLocation;
    }

    private static X509Certificate loadCertificateFromFile(Path caCertPath) throws IOException, CertificateException {
        PEMParser pemParser = new PEMParser(new FileReader(caCertPath.toFile()));
        X509CertificateHolder certificateHolder = (X509CertificateHolder)pemParser.readObject();
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        X509Certificate certificate = converter.getCertificate(certificateHolder);
        return certificate;
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
        KeyPairAndCertificate caDetails = generateTopCa(topDomain);


        Path keyPath = outputFolder.resolve(String.format("ca.%s.key.pem", topDomain.getDomainName()));
        FileWriter keyOut = new FileWriter(keyPath.toFile());
        JcaPEMWriter keyWriter = new JcaPEMWriter(keyOut);
        keyWriter.writeObject(caDetails.getKeyPair());
        keyWriter.close();
        keyOut.close();
        Path certPath = outputFolder.resolve(String.format("ca.%s.crt.pem", topDomain.getDomainName()));
        FileWriter certOut = new FileWriter(certPath.toFile());
        JcaPEMWriter certWriter = new JcaPEMWriter(certOut);
        certWriter.writeObject(caDetails.getCertificate());
        certWriter.close();
        certOut.close();
        return new CertKeyPair(certPath,keyPath);



    }

    private static KeyPairAndCertificate generateTopCa(TopDomain topDomain) throws NoSuchAlgorithmException, CertIOException, OperatorCreationException, CertificateException {
        SecureRandom secureRandom = new SecureRandom();
        KeyPair keyPair = generateKeyPair(4096);
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

        KeyPairAndCertificate caDetails = new KeyPairAndCertificate(keyPair,cert);
        return caDetails;
    }

    private static KeyPair generateKeyPair(int keysize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keysize);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
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

    private static class KeyPairAndCertificate {
        private KeyPair keyPair;
        private X509Certificate certificate;

        private KeyPairAndCertificate(KeyPair keyPair, X509Certificate certificate) {
            this.keyPair = keyPair;
            this.certificate = certificate;
        }

        public KeyPair getKeyPair() {
            return keyPair;
        }

        public X509Certificate getCertificate() {
            return certificate;
        }
    }
    
    
    private static class KeyPairAndCsr {
        private KeyPair keyPair;
        
        private PKCS10CertificationRequest csr;

        private KeyPairAndCsr(KeyPair keyPair, PKCS10CertificationRequest csr) {
            this.keyPair = keyPair;
            this.csr = csr;
        }

        public KeyPair getKeyPair() {
            return keyPair;
        }

        public PKCS10CertificationRequest getCsr() {
            return csr;
        }
    }

    private static class CertificateAndCertificateChain {
        private X509Certificate certificate;
        private List<X509Certificate> chain;

        private CertificateAndCertificateChain(X509Certificate certificate, List<X509Certificate> chain) {
            this.certificate = certificate;
            this.chain = chain;
        }


        public X509Certificate getCertificate() {
            return certificate;
        }

        public List<X509Certificate> getChain() {
            return chain;
        }
    }

    private static class CertificateCertificateChainAndKeys {
        private KeyPair keyPair;

        private X509Certificate certificate;

        private List<X509Certificate> certificateChain;


        private CertificateCertificateChainAndKeys(KeyPair keyPair, X509Certificate certificate, List<X509Certificate> certificateChain) {
            this.keyPair = keyPair;
            this.certificate = certificate;
            this.certificateChain = certificateChain;
        }

        public KeyPair getKeyPair() {
            return keyPair;
        }

        public X509Certificate getCertificate() {
            return certificate;
        }

        public List<X509Certificate> getCertificateChain() {
            return certificateChain;
        }
    }
}
