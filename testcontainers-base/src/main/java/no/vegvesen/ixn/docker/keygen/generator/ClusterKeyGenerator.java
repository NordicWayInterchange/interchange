package no.vegvesen.ixn.docker.keygen.generator;

import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.docker.keygen.*;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX500NameUtil;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
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
            saveCert(outputFolder,String.format("int.%s.crt.pem",domain.getDomainName()),intermediateCert.getCertificate());
            saveCertChain(outputFolder,String.format("chain.int.%s.crt.pem",domain.getDomainName()),intermediateCert.getChain());
            saveKeyPair(outputFolder,String.format("int.%s.key.pem",domain.getDomainName()),intermediateCsr.getKeyPair());
            Interchange interchange = domain.getInterchange();
            for (AdditionalHost host : interchange.getAdditionalHosts()) {
                CertificateCertificateChainAndKeys additionalhostKeys = generateServerCertForHost(host, intermediateCert.getCertificate(), intermediateCert.getChain(), intermediateCsr.getKeyPair().getPrivate());
                saveCert(outputFolder, String.format("%s.crt.pem", host.getHostname()), additionalhostKeys.getCertificate());
                saveCertChain(outputFolder, String.format("chain.%s.crt.pem", host.getHostname()), additionalhostKeys.getCertificateChain());
                saveKeyPair(outputFolder, String.format("%s.key.pem", host.getHostname()),additionalhostKeys.getKeyPair());
                //generateServerCertForHost(outputFolder, host, intermediateCertOnHost, intermediateCertChainOnHost, intermediateKeyPath);
                //TODO create keyStore for host (using chain, I expect)
            }
            for (ServicProviderDescription description : interchange.getServiceProviders()) {
                KeyPairAndCsr spCsr = generateCsrForServiceProviderBC(description);
                List<X509Certificate> spCertChain = generateServiceProviderCertBC(spCsr.getCsr(), topCa.getCertificate(), intermediateCert.getCertificate(), intermediateCsr.getKeyPair().getPrivate(),description.getName());
                String serviceProviderKeystorePassword = generatePassword(random,24);
                String serviceProviderKeystoreName = description.getName() + ".p12";
                Files.writeString(outputFolder.resolve(description.getName() + ".txt"),serviceProviderKeystorePassword);
                X509Certificate[] certChain = spCertChain.toArray(new X509Certificate[0]);
                generateKeystoreBC(outputFolder,serviceProviderKeystorePassword,serviceProviderKeystoreName, description.getName(), spCsr.getKeyPair().getPrivate(), certChain);
                saveCert(outputFolder,String.format("%s.crt.pem",description.getName()),spCertChain.get(0));
                saveCertChain(outputFolder,String.format("chain.%s.crt.pem",description.getName()),spCertChain);
                saveKeyPair(outputFolder,String.format("%s.key.pem",description.getName()),spCsr.getKeyPair());
            }

        }
    }

    public static List<X509Certificate> generateServiceProviderCertBC(PKCS10CertificationRequest certificationRequest, X509Certificate caCertificate, X509Certificate intermediateCertificate, PrivateKey privateKey, String cn) {
        try {
            CertSigner certSigner = new CertSigner(privateKey, intermediateCertificate, caCertificate);
            List<X509Certificate> certChain = certSigner.sign(certificationRequest,cn);
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


    public static Path generateKeystoreBC(Path outputFolder, String keystorePassword, String keystoreName, String entryName, PrivateKey privateKey, Certificate[] certificates) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null,null);
        keyStore.setKeyEntry(entryName, privateKey,keystorePassword.toCharArray(), certificates);

        Path outputPath = outputFolder.resolve(keystoreName);
        keyStore.store(new FileOutputStream(outputPath.toFile()),keystorePassword.toCharArray());
        return outputPath;
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


    public static Path makeTrustStore(String topDomainTruststorePassword, Path outputFolder, String truststoreName, String caCertAlias, X509Certificate certificate) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null,null);
        trustStore.setCertificateEntry(caCertAlias, certificate);
        Path storeLocation = outputFolder.resolve(truststoreName);
        trustStore.store(new FileOutputStream(storeLocation.toFile()), topDomainTruststorePassword.toCharArray());
        return storeLocation;
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
