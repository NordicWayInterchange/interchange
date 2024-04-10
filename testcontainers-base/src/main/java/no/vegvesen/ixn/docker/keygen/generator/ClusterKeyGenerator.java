package no.vegvesen.ixn.docker.keygen.generator;

import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.docker.keygen.AdditionalHost;
import no.vegvesen.ixn.docker.keygen.Cluster;
import no.vegvesen.ixn.docker.keygen.Interchange;
import no.vegvesen.ixn.docker.keygen.IntermediateDomain;
import no.vegvesen.ixn.docker.keygen.ServicProviderDescription;
import no.vegvesen.ixn.docker.keygen.TopDomain;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX500NameUtil;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import javax.security.auth.x500.X500Principal;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClusterKeyGenerator {

    private static final char[] allowedChars = {
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '0','1','2','3','4','5','6','7','8','9',
            '*','-','_','$','+'
    };


    public static void generateKeys(Cluster cluster, Path outputFolder) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, OperatorCreationException, NoSuchProviderException, SignatureException, InvalidKeyException {
        Files.createDirectories(outputFolder);
        SecureRandom random = new SecureRandom();
        TopDomain topDomain = cluster.getTopDomain();
        CertificateCertificateChainAndKeys topCa = generateTopCa(topDomain.getDomainName(), topDomain.getOwnerCountry(), random);
        Path caKeyPath = outputFolder.resolve(String.format("ca.%s.key.pem", topDomain.getDomainName()));
        saveKeyPair(topCa.keyPair(), new FileWriter(caKeyPath.toFile()));
        Path caCertPath = outputFolder.resolve(String.format("ca.%s.crt.pem", topDomain.getDomainName()));
        saveCert(topCa.certificate(), new FileWriter(caCertPath.toFile()));
        String topDomainTrustStorePassword = generatePassword(random,24);
        String caCertAlias = "myKey";
        Path truststorePath = outputFolder.resolve(topDomain.getDomainName() + "_truststore.jks");
        makeTrustStore(topDomainTrustStorePassword, caCertAlias,topCa.certificate(), new FileOutputStream(truststorePath.toFile()));

        String passwordFile = topDomain.getDomainName() + "_truststore.txt";
        Path truststorePasswordPath = outputFolder.resolve(passwordFile);
        Files.writeString(truststorePasswordPath,topDomainTrustStorePassword);
        for (IntermediateDomain domain : topDomain.getIntermediateDomains()) {
            //TODO this should generate TWO key/cert paris.
            //One for the CA
            //One for the host at the intermediate domain.
            CertificateCertificateChainAndKeys intermediateCa = generateIntermediateCA(domain.getDomainName(), domain.getOwningCountry(), new ArrayList<>(), topCa.certificate(), topCa.keyPair().getPrivate(), random);
            String intermediateCaKeystorePassword = generatePassword(random,24);
            String keystoreName = domain.getDomainName() + ".p12";
            Path keystorePath = outputFolder.resolve(keystoreName);
            generateKeystoreBC(intermediateCaKeystorePassword, domain.getDomainName(), intermediateCa.keyPair().getPrivate(), intermediateCa.certificateChain().toArray(new X509Certificate[0]), new FileOutputStream(keystorePath.toFile()));
            Files.writeString(outputFolder.resolve(domain.getDomainName() + ".txt"),intermediateCaKeystorePassword);
            Path intermediateCertPath = outputFolder.resolve(String.format("int.%s.crt.pem", domain.getDomainName()));
            saveCert(intermediateCa.certificate(), new FileWriter(intermediateCertPath.toFile()));
            Path intermediateCertChainPath = outputFolder.resolve(String.format("chain.int.%s.crt.pem", domain.getDomainName()));
            saveCertChain(intermediateCa.certificateChain(), new FileWriter(intermediateCertChainPath.toFile()));
            Path intermediateKeyPath = outputFolder.resolve(String.format("int.%s.key.pem", domain.getDomainName()));
            saveKeyPair(intermediateCa.keyPair(), new FileWriter(intermediateKeyPath.toFile()));
            Interchange interchange = domain.getInterchange();
            for (AdditionalHost host : interchange.getAdditionalHosts()) {
                CertificateCertificateChainAndKeys additionalHostKeys = generateServerCertForHost(host.getHostname(), intermediateCa.certificate(), intermediateCa.certificateChain(), intermediateCa.keyPair().getPrivate(), random);
                Path hostCertPath = outputFolder.resolve(String.format("%s.crt.pem", host.getHostname()));
                saveCert(additionalHostKeys.certificate(), new FileWriter(hostCertPath.toFile()));
                Path hostChainCertPath = outputFolder.resolve(String.format("chain.%s.crt.pem", host.getHostname()));
                saveCertChain(additionalHostKeys.certificateChain(), new FileWriter(hostChainCertPath.toFile()));
                Path hostKeyPath = outputFolder.resolve(String.format("%s.key.pem", host.getHostname()));
                saveKeyPair(additionalHostKeys.keyPair(), new FileWriter(hostKeyPath.toFile()));
                //generateServerCertForHost(outputFolder, host, intermediateCertOnHost, intermediateCertChainOnHost, intermediateKeyPath);
                //TODO create keyStore for host (using chain, I expect)
            }
            for (ServicProviderDescription description : interchange.getServiceProviders()) {
                generateServiceProviderKeys(outputFolder, description, random, topCa.certificate(), intermediateCa.certificate(), intermediateCa.keyPair().getPrivate());
            }

        }
    }

    //TODO this could use a List of certificates. Much easier.
    public static void generateServiceProviderKeys(Path outputFolder, ServicProviderDescription description, SecureRandom random, X509Certificate topCaCertificate, X509Certificate intermediateCertificate, PrivateKey intermediatePrivateKey) throws NoSuchAlgorithmException, OperatorCreationException, IOException, KeyStoreException, CertificateException {
        KeyPairAndCsr spCsr = generateCsrForServiceProviderBC(description.getName(), description.getCountry());
        List<X509Certificate> spCertChain = generateServiceProviderCertBC(spCsr.csr(), topCaCertificate, intermediateCertificate, intermediatePrivateKey, description.getName());
        String serviceProviderKeystorePassword = generatePassword(random,24);
        String serviceProviderKeystoreName = description.getName() + ".p12";
        Files.writeString(outputFolder.resolve(description.getName() + ".txt"),serviceProviderKeystorePassword);
        X509Certificate[] certChain = spCertChain.toArray(new X509Certificate[0]);
        generateKeystoreBC(serviceProviderKeystorePassword, description.getName(), spCsr.keyPair().getPrivate(), certChain, new FileOutputStream(outputFolder.resolve(serviceProviderKeystoreName).toFile()));
        Path serviceProviderCertPath = outputFolder.resolve(String.format("%s.crt.pem", description.getName()));
        saveCert(spCertChain.get(0), new FileWriter(serviceProviderCertPath.toFile()));
        Path serviceProviderChainCertPath = outputFolder.resolve(String.format("chain.%s.crt.pem", description.getName()));
        saveCertChain(spCertChain, new FileWriter(serviceProviderChainCertPath.toFile()));
        Path serviceProviderKeyPath = outputFolder.resolve(String.format("%s.key.pem", description.getName()));
        saveKeyPair(spCsr.keyPair(), new FileWriter(serviceProviderKeyPath.toFile()));
    }

    public static List<X509Certificate> generateServiceProviderCertBC(PKCS10CertificationRequest certificationRequest, X509Certificate caCertificate, X509Certificate intermediateCertificate, PrivateKey privateKey, String cn) {
        try {
            CertSigner certSigner = new CertSigner(privateKey, intermediateCertificate, caCertificate);
            return certSigner.sign(certificationRequest,cn);
        } catch (IOException | NoSuchAlgorithmException | CertificateException | OperatorCreationException |
                 SignatureException | InvalidKeyException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }

    }

    public static KeyPairAndCsr generateCsrForServiceProviderBC(String name, String country) throws NoSuchAlgorithmException, OperatorCreationException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        X500Principal x500Principal = new X500Principal(
                String.format(
                        "emailAddress=test@test.com, CN=%s, O=Nordic Way, C=%s",
                        name,
                        country
                )
        );
        PKCS10CertificationRequest csr = createCertificateRequest(x500Principal, keyPair, "SHA256withRSA");
        return new KeyPairAndCsr(keyPair,csr);
    }


    public static CertificateCertificateChainAndKeys generateServerCertForHost(String hostname, X509Certificate issuerCertificate, List<X509Certificate> issuerCertificateChain, PrivateKey issuerPrivateKey, SecureRandom secureRandom) throws NoSuchAlgorithmException, OperatorCreationException, CertIOException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException {
        KeyPair keyPair = generateKeyPair(2048);
        X500Principal subject = new X500Principal(String.format(
                "O=Nordic Way, CN=%s",
                hostname
        ));
        PKCS10CertificationRequest csr = createCertificateRequest(subject, keyPair, "SHA512withRSA");
        SubjectPublicKeyInfo csrSubjectPublicKeyInfo = csr.getSubjectPublicKeyInfo();
        BigInteger serialNumber = new BigInteger(Long.toString(secureRandom.nextLong()));
        Date startDate = new Date();
        Date toDate = Date.from(LocalDateTime.now().plusYears(1).atZone(ZoneId.systemDefault()).toInstant());
        X500Name issuerSubject = JcaX500NameUtil.getSubject(issuerCertificate);
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
        AuthorityKeyIdentifier authorityKeyIdentifier = extensionUtils.createAuthorityKeyIdentifier(issuerCertificate.getPublicKey());
        certificateBuilder.addExtension(Extension.authorityKeyIdentifier,false, authorityKeyIdentifier);
        SubjectKeyIdentifier subjectKeyIdentifier = extensionUtils.createSubjectKeyIdentifier(csrSubjectPublicKeyInfo);
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier,false, subjectKeyIdentifier);
        KeyPurposeId[] keyPurposeIds = new KeyPurposeId[] {KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth};
        ExtendedKeyUsage extendedKeyUsage = new ExtendedKeyUsage(keyPurposeIds);
        certificateBuilder.addExtension(Extension.extendedKeyUsage,false,extendedKeyUsage);

        ArrayList<GeneralName> names = new ArrayList<>();
        names.add(new GeneralName(GeneralName.dNSName,hostname));
        GeneralNames subjectAltNames = new GeneralNames(names.toArray(new GeneralName[0]));
        certificateBuilder.addExtension(Extension.subjectAlternativeName,false,subjectAltNames);
        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA512withRSA");

        ContentSigner signer = signerBuilder.build(issuerPrivateKey);
        X509CertificateHolder issuedCertHolder = certificateBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(issuedCertHolder);
        //TODO do we need BC here?
        certificate.verify(issuerCertificate.getPublicKey());
        List<X509Certificate> certificateChain = new ArrayList<>(issuerCertificateChain);
        certificateChain.add(0,certificate);
        return new CertificateCertificateChainAndKeys(keyPair,certificate,certificateChain);

    }


    public static void generateKeystoreBC(String keystorePassword, String entryName, PrivateKey privateKey, Certificate[] certificates, OutputStream stream) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null,null);
        keyStore.setKeyEntry(entryName, privateKey,keystorePassword.toCharArray(), certificates);

        keyStore.store(stream,keystorePassword.toCharArray());
    }

    private static void saveCertChain(List<X509Certificate> certificateChain, Writer writer) throws IOException {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(writer)){
            for (X509Certificate cert : certificateChain) {
                pemWriter.writeObject(cert);
            }
        }
    }

    private static void saveCert(X509Certificate certificate, Writer writer) throws IOException {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
            pemWriter.writeObject(certificate);
        }
    }


    public static CertificateAndCertificateChain signIntermediateCsr(X509Certificate caCert, List<X509Certificate> certChain,PrivateKey caPrivateKey, PKCS10CertificationRequest csr, SecureRandom secureRandom) throws NoSuchAlgorithmException, CertIOException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException, OperatorCreationException {
        SubjectPublicKeyInfo csrSubjectPublicKeyInfo = csr.getSubjectPublicKeyInfo();
        BigInteger serialNumber = new BigInteger(Long.toString(secureRandom.nextLong()));
        Date startDate = new Date();
        Date toDate = Date.from(LocalDateTime.now().plusYears(1).atZone(ZoneId.systemDefault()).toInstant());

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

        ContentSigner signer = signerBuilder.build(caPrivateKey);


        X509CertificateHolder issuedCertHolder = certificateBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(issuedCertHolder);
        //TODO do we need BC here?
        certificate.verify(caCert.getPublicKey());

        ArrayList<X509Certificate> newCertChain = new ArrayList<>(certChain);
        newCertChain.add(certificate);

        return new CertificateAndCertificateChain(certificate, newCertChain);
    }


    private static void saveKeyPair(KeyPair keyPair, Writer keyWriter) throws IOException {
        JcaPEMWriter pemWriter;
        pemWriter = new JcaPEMWriter(keyWriter);
        pemWriter.writeObject(keyPair);
        pemWriter.close();
    }

    public static KeyPairAndCsr generateIntermediateKeypairAndCsr(String domainName, String owningCountry) throws NoSuchAlgorithmException, OperatorCreationException {
        KeyPair keyPair = generateKeyPair(4096);
        PKCS10CertificationRequest csr = createCsr(domainName, owningCountry, keyPair);
        return new KeyPairAndCsr(keyPair,csr);
    }

    private static PKCS10CertificationRequest createCsr(String domainName, String owningCountry, KeyPair keyPair) throws OperatorCreationException {
        if (owningCountry == null) {
            owningCountry = "NO";
        }
        X500Principal x500Principal = new X500Principal(
                String.format(
                        "CN=%s, O=Nordic Way, C=%s",
                        domainName,
                        owningCountry
                )
        );
        return createCertificateRequest(x500Principal, keyPair, "SHA512withRSA");
    }

    private static PKCS10CertificationRequest createCertificateRequest(X500Principal subject, KeyPair keyPair, String signAlgorithm) throws OperatorCreationException {
        JcaPKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
        JcaContentSignerBuilder signBuilder = new JcaContentSignerBuilder(signAlgorithm);
        ContentSigner signer = signBuilder.build(keyPair.getPrivate());
        return builder.build(signer);
    }


    public static void makeTrustStore(String topDomainTruststorePassword, String caCertAlias, X509Certificate certificate, OutputStream stream) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null,null);
        trustStore.setCertificateEntry(caCertAlias, certificate);
        trustStore.store(stream, topDomainTruststorePassword.toCharArray());
    }

    public static CertificateCertificateChainAndKeys generateTopCa(String commonName, String ownerCountry, SecureRandom secureRandom) throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, CertIOException {
        if (ownerCountry == null) {
            ownerCountry = "NO";
        }
        KeyPair keyPair = generateKeyPair(4096);
        X500Principal x500Principal = new X500Principal(
                String.format(
                        "CN=%s, O=Nordic Way, C=%s",
                        commonName,
                        ownerCountry
                )
        );
        JcaContentSignerBuilder signBuilder = new JcaContentSignerBuilder("SHA512withRSA");
        BigInteger serial = new BigInteger(Long.toString(secureRandom.nextLong()));
        Date fromDate = new Date();
        Date toDate = Date.from(LocalDateTime.now().plusYears(1).atZone(ZoneId.systemDefault()).toInstant());
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

        KeyPairAndCertificate details = new KeyPairAndCertificate(keyPair, cert);
        ArrayList<X509Certificate> certificates = new ArrayList<>();
        certificates.add(details.certificate());
        return new CertificateCertificateChainAndKeys(details.keyPair(), details.certificate(),certificates);
    }

    //Details of the cert being made, (Should we make the X500Name straight away, maybe)?
    public static CertificateCertificateChainAndKeys generateIntermediateCA(String commonName, String country, List<X509Certificate> issuerCertChain, X509Certificate issuerCert, PrivateKey issuerKey, SecureRandom secureRandom) throws NoSuchAlgorithmException, OperatorCreationException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException, CertIOException {
        KeyPairAndCsr intermediateCsr = generateIntermediateKeypairAndCsr(commonName, country);
        CertificateAndCertificateChain intermediateCert = signIntermediateCsr(issuerCert, issuerCertChain,issuerKey, intermediateCsr.csr(), secureRandom);
        return new CertificateCertificateChainAndKeys(intermediateCsr.keyPair(),intermediateCert.certificate(),intermediateCert.chain());
    }

    //TODO this could return a list of certificates if a chain.
    public static X509Certificate loadSingleCertificate(Path certificatePath) throws IOException, CertificateException {
        PEMParser certificateParser = new PEMParser(new FileReader(certificatePath.toFile()));
        X509CertificateHolder certificateHolder = (X509CertificateHolder) certificateParser.readObject();
        return new JcaX509CertificateConverter().getCertificate(certificateHolder);
    }

    public static X509Certificate[] loadCertificateChain(Path certificatePath) throws IOException, CertificateException {
        PEMParser parser = new PEMParser(new FileReader(certificatePath.toFile()));
        Object o = parser.readObject();
        List<X509Certificate> certificates = new ArrayList<>();
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        while (o  != null) {
            certificates.add(converter.getCertificate((X509CertificateHolder) o));
            o = parser.readObject();
        }
        return certificates.toArray(new X509Certificate[0]);
    }

    public static PrivateKey loadPrivateKey(Path pemKeyPath) throws IOException {
        PEMParser keyParser = new PEMParser(new FileReader(pemKeyPath.toFile()));
        return new JcaPEMKeyConverter().getPrivateKey((PrivateKeyInfo) keyParser.readObject());

    }

    public static KeyPair loadKeyPair(Path pemKeyPath) throws IOException {
        PEMParser parser = new PEMParser(new FileReader(pemKeyPath.toFile()));
        return new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) parser.readObject());
    }


    public record KeyPairAndCertificate(KeyPair keyPair, X509Certificate certificate) {
    }

    private static KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.generateKeyPair();
    }

    public static String generatePassword(SecureRandom random, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i =  0; i < length; i++) {
            builder.append(allowedChars[random.nextInt(allowedChars.length)]);
        }
        return builder.toString();
    }



    public record KeyPairAndCsr(KeyPair keyPair, PKCS10CertificationRequest csr) {
    }

    public record CertificateAndCertificateChain(X509Certificate certificate, List<X509Certificate> chain) {
    }

    public record CertificateCertificateChainAndKeys(KeyPair keyPair, X509Certificate certificate, List<X509Certificate> certificateChain) {
    }
}
