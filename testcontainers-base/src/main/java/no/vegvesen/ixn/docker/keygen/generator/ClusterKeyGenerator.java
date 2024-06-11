package no.vegvesen.ixn.docker.keygen.generator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.docker.keygen.*;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
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
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import java.io.*;
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
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class ClusterKeyGenerator {

    private static final char[] allowedChars = {
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '0','1','2','3','4','5','6','7','8','9',
            '*','-','_','$','+'
    };


    public static void generateKeys(Cluster cluster, Path outputFolder, PasswordGenerator passwordGenerator) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, OperatorCreationException, NoSuchProviderException, SignatureException, InvalidKeyException {
        Files.createDirectories(outputFolder);
        SecureRandom random = new SecureRandom();
        TopDomain topDomain = cluster.getTopDomain();
        CertificateCertificateChainAndKeys topCa = generateTopCa(topDomain.getDomainName(), topDomain.getOwnerCountry(), random);
        Path caKeyPath = outputFolder.resolve(String.format("ca.%s.key.pem", topDomain.getDomainName()));
        saveKeyPair(topCa.keyPair(), new FileWriter(caKeyPath.toFile()));
        Path caCertPath = outputFolder.resolve(String.format("ca.%s.crt.pem", topDomain.getDomainName()));
        saveCert(topCa.certificate(), new FileWriter(caCertPath.toFile()));
        String topDomainTrustStorePassword = passwordGenerator.generatePassword();
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
            String intermediateCaKeystorePassword = passwordGenerator.generatePassword();
            String keystoreName = domain.getDomainName() + ".p12";
            Path keystorePath = outputFolder.resolve(keystoreName);
            generateKeystore(intermediateCaKeystorePassword, domain.getDomainName(), intermediateCa.keyPair().getPrivate(), intermediateCa.certificateChain(), new FileOutputStream(keystorePath.toFile()));
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
                String hostKeystorePassword = passwordGenerator.generatePassword();
                Path hostKeyPasswordPath = outputFolder.resolve(host.getHostname() + ".p12");
                generateKeystore(hostKeystorePassword,host.getHostname(),additionalHostKeys.keyPair().getPrivate(), additionalHostKeys.certificateChain(), new FileOutputStream(hostKeyPasswordPath.toFile()));
                Files.writeString(outputFolder.resolve(host.getHostname() + ".txt"),hostKeystorePassword);

            }
            for (ServicProviderDescription description : interchange.getServiceProviders()) {
                generateServiceProviderKeys(outputFolder, description, intermediateCa.keyPair().getPrivate(), intermediateCa.certificate(), intermediateCa.certificateChain(), passwordGenerator);
            }

        }
    }

    public static CaResponse generate(CARequest caRequest) throws CertificateException, NoSuchAlgorithmException, SignatureException, OperatorCreationException, InvalidKeyException, NoSuchProviderException, CertIOException {
        SecureRandom random = new SecureRandom();
        CertificateCertificateChainAndKeys topCa = generateTopCa(caRequest.name(), caRequest.country(), random);
        List<HostResponse> hostResponses = getHostResponses(caRequest.hostRequests(), topCa, random);
        List<ClientResponse> clientResponses = getClientResponses(caRequest.clientRequests(), topCa);

        List<CaResponse> responses = new ArrayList<>();
        for (CARequest request : caRequest.subCaRequests()) {
            CaResponse response = generate(request, topCa, random);
            responses.add(response);
        }
        return new CaResponse(topCa, caRequest.name(), hostResponses, clientResponses,responses);

    }

    private static CaResponse generate(CARequest caRequest, CertificateCertificateChainAndKeys parentCa, SecureRandom random) throws CertificateException, NoSuchAlgorithmException, SignatureException, OperatorCreationException, InvalidKeyException, NoSuchProviderException, CertIOException {
        CertificateCertificateChainAndKeys intermediateCa = generateIntermediateCA(caRequest.name(), caRequest.country(), parentCa.certificateChain(), parentCa.certificate(), parentCa.keyPair().getPrivate(), random);
        List<ClientResponse> clientResponses = getClientResponses(caRequest.clientRequests(), intermediateCa);
        List<HostResponse> hostResponses = getHostResponses(caRequest.hostRequests(),intermediateCa,random);
        List<CaResponse> responses = new ArrayList<>();
        for (CARequest request : caRequest.subCaRequests()) {
            CaResponse response = generate(request, intermediateCa, random);
            responses.add(response);
        }
        return new CaResponse(intermediateCa, caRequest.name(), hostResponses, clientResponses,responses);
    }

    /* Makes a truststore for the top CA, and keystores for each host and client in the chain */
    public static void store(CaResponse response, Path basePath, PasswordGenerator passwordGenerator) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        trustStoreForCa(response, basePath, passwordGenerator);
        for (CaResponse subResponses : response.caResponses()) {
            store(subResponses,basePath, passwordGenerator);
        }
        storeHostAndClientStores(response, basePath, passwordGenerator);
    }

    private static void trustStoreForCa(CaResponse response, Path basePath, PasswordGenerator passwordGenerator) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        String password = passwordGenerator.generatePassword();
        Files.writeString(basePath.resolve(response.name() + ".txt"),password);
        try (OutputStream outputStream = Files.newOutputStream(basePath.resolve(response.name() + ".jks"))) {
            makeTrustStore(response, password,outputStream);
        }
    }


    private static void storeHostAndClientStores(CaResponse response, Path basePath, PasswordGenerator randomPasswordGenerator) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {

        for (HostResponse hostResponse : response.hostResponses()) {
            String hostPassword = randomPasswordGenerator.generatePassword();
            Files.writeString(basePath.resolve(hostResponse.host() + ".txt"),hostPassword);
            makeKeystore(hostResponse,hostPassword,Files.newOutputStream(basePath.resolve(hostResponse.host() + ".p12")));
        }
        for (ClientResponse clientResponse : response.clientResponses()) {
            String clientPassword = randomPasswordGenerator.generatePassword();
            Files.writeString(basePath.resolve(clientResponse.name() + ".txt"),clientPassword);
            makeKeystore(clientResponse,clientPassword,Files.newOutputStream(basePath.resolve(clientResponse.name() + ".p12")));
        }
    }

    private static List<ClientResponse> getClientResponses(List<ClientRequest> clients, CertificateCertificateChainAndKeys issuer) throws NoSuchAlgorithmException, OperatorCreationException, CertIOException, CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {
        ArrayList<ClientResponse> clientResponses = new ArrayList<>();
        for (ClientRequest clientRequest : clients) {
            CertificateCertificateChainAndKeys clientDetails = generateSPKeys(clientRequest.name(), clientRequest.country(), clientRequest.email(), issuer.certificate(), issuer.keyPair().getPrivate(), issuer.certificateChain());
            clientResponses.add(new ClientResponse(clientRequest.name(),clientDetails));
        }
        return clientResponses;
    }

    private static List<HostResponse> getHostResponses(List<HostRequest> hosts, CertificateCertificateChainAndKeys issuer, SecureRandom random) throws NoSuchAlgorithmException, OperatorCreationException, CertIOException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException {
        List<HostResponse> hostResponses = new ArrayList<>();
        for (HostRequest hostRequest : hosts) {
            CertificateCertificateChainAndKeys hostDetails = generateServerCertForHost(hostRequest.name(), issuer.certificate(), issuer.certificateChain(), issuer.keyPair().getPrivate(), random);
            hostResponses.add(new HostResponse(hostRequest.name(), hostDetails));
        }
        return hostResponses;
    }

    public static void generateServiceProviderKeys(Path outputFolder, ServicProviderDescription description, PrivateKey issuerPrivateKey, X509Certificate issuerCertificate, List<X509Certificate> issuerCertChain, PasswordGenerator passwordGenerator) throws NoSuchAlgorithmException, OperatorCreationException, IOException, KeyStoreException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException {
        CertificateCertificateChainAndKeys result = generateSPKeys(description.getName(), description.getCountry(), "test@test.com", issuerCertificate, issuerPrivateKey, issuerCertChain);
        String serviceProviderKeystorePassword = passwordGenerator.generatePassword();
        String serviceProviderKeystoreName = description.getName() + ".p12";
        Files.writeString(outputFolder.resolve(description.getName() + ".txt"),serviceProviderKeystorePassword);
        generateKeystore(serviceProviderKeystorePassword, description.getName(), result.keyPair().getPrivate(), result.certificateChain(), new FileOutputStream(outputFolder.resolve(serviceProviderKeystoreName).toFile()));
        Path serviceProviderCertPath = outputFolder.resolve(String.format("%s.crt.pem", description.getName()));
        saveCert(result.certificate(), new FileWriter(serviceProviderCertPath.toFile()));
        Path serviceProviderChainCertPath = outputFolder.resolve(String.format("chain.%s.crt.pem", description.getName()));
        saveCertChain(result.certificateChain(), new FileWriter(serviceProviderChainCertPath.toFile()));
        Path serviceProviderKeyPath = outputFolder.resolve(String.format("%s.key.pem", description.getName()));
        saveKeyPair(result.keyPair(), new FileWriter(serviceProviderKeyPath.toFile()));
    }

    public static CertificateCertificateChainAndKeys generateSPKeys(String commonName, String spCountry, String spEmail, X509Certificate issuerCertificate, PrivateKey issuerPrivateKey, List<X509Certificate> issuerCertChain) throws NoSuchAlgorithmException, OperatorCreationException, CertIOException, CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {
        KeyPairAndCsr spCsr = generateCsrForServiceProviderBC(commonName, spCountry, spEmail);
        CertSigner certSigner = new CertSigner(issuerPrivateKey, issuerCertificate, issuerCertChain);
        List<X509Certificate> newCertChain = certSigner.sign(spCsr.csr(), commonName);
        return new CertificateCertificateChainAndKeys(spCsr.keyPair(), newCertChain.get(0),newCertChain);
    }

    public static KeyPairAndCsr generateCsrForServiceProviderBC(String name, String country, String email) throws NoSuchAlgorithmException, OperatorCreationException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        X500Name x500Name = new X500Name(
                String.format(
                        "emailAddress=%s, CN=%s, O=Nordic Way, C=%s",
                        email,
                        name,
                        country
                )
        );
        PKCS10CertificationRequest csr = createCertificateRequest(x500Name, keyPair);
        return new KeyPairAndCsr(keyPair,csr);
    }


    public static CertificateCertificateChainAndKeys generateServerCertForHost(String hostname, X509Certificate issuerCertificate, List<X509Certificate> issuerCertificateChain, PrivateKey issuerPrivateKey, SecureRandom secureRandom) throws NoSuchAlgorithmException, OperatorCreationException, CertIOException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException {
        KeyPair keyPair = generateKeyPair(2048);
        X500Name subject = new X500Name(
                String.format(
                        "O=Nordic Way, CN=%s",
                        hostname
                )

        );
        PKCS10CertificationRequest csr = createCertificateRequest(subject, keyPair);
        JcaPKCS10CertificationRequest csrWrapper = new JcaPKCS10CertificationRequest(csr);
        PublicKey subjectPublicKey = csrWrapper.getPublicKey();
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
                subjectPublicKey
        );
        certificateBuilder.addExtension(Extension.basicConstraints,true,new BasicConstraints(false));
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.nonRepudiation | KeyUsage.keyEncipherment);
        certificateBuilder.addExtension(Extension.keyUsage,true,keyUsage);
        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
        AuthorityKeyIdentifier authorityKeyIdentifier = extensionUtils.createAuthorityKeyIdentifier(issuerCertificate.getPublicKey());
        certificateBuilder.addExtension(Extension.authorityKeyIdentifier,false, authorityKeyIdentifier);
        SubjectKeyIdentifier subjectKeyIdentifier = extensionUtils.createSubjectKeyIdentifier(subjectPublicKey);
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier,false, subjectKeyIdentifier);
        KeyPurposeId[] keyPurposeIds = new KeyPurposeId[] {KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth};
        ExtendedKeyUsage extendedKeyUsage = new ExtendedKeyUsage(keyPurposeIds);
        certificateBuilder.addExtension(Extension.extendedKeyUsage,false,extendedKeyUsage);

        //if (! "localhost".equals(hostname)) {
            ArrayList<GeneralName> names = new ArrayList<>();
            names.add(new GeneralName(GeneralName.dNSName, hostname));
            GeneralNames subjectAltNames = new GeneralNames(names.toArray(new GeneralName[0]));
            certificateBuilder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);
        //}
        JcaContentSignerBuilder signerBuilder = createContentSignerBuilder();

        ContentSigner signer = signerBuilder.build(issuerPrivateKey);
        X509CertificateHolder issuedCertHolder = certificateBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(issuedCertHolder);
        //TODO do we need BC here?
        certificate.verify(issuerCertificate.getPublicKey());
        List<X509Certificate> certificateChain = new ArrayList<>();
        certificateChain.add(certificate);
        certificateChain.addAll(issuerCertificateChain);
        return new CertificateCertificateChainAndKeys(keyPair,certificate,certificateChain);

    }

    public static void makeKeystore(HostResponse hostResponse,String password, OutputStream outputStream) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null,null);
        X509Certificate[] certificates = hostResponse.keyDetails().certificateChain().toArray(new X509Certificate[0]);
        keyStore.setKeyEntry(hostResponse.host(),hostResponse.keyDetails().keyPair().getPrivate(),password.toCharArray(),certificates);
        keyStore.store(outputStream,password.toCharArray());
    }

    public static void makeKeystore(ClientResponse clientResponse,String password, OutputStream outputStream) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null,null);
        X509Certificate[] certificates = clientResponse.clientDetails().certificateChain().toArray(new X509Certificate[0]);
        keyStore.setKeyEntry(clientResponse.name(),clientResponse.clientDetails().keyPair().getPrivate(),password.toCharArray(),certificates);
        keyStore.store(outputStream,password.toCharArray());
    }

    public static void makeTrustStore(CaResponse caResponse, String truststorePassword, OutputStream outputStream) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null,null);
        trustStore.setCertificateEntry("myKey",caResponse.details().certificate());
        trustStore.store(outputStream,truststorePassword.toCharArray());
    }

    public static void makeTrustStore(String topDomainTruststorePassword, String caCertAlias, X509Certificate certificate, OutputStream stream) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null,null);
        trustStore.setCertificateEntry(caCertAlias, certificate);
        trustStore.store(stream, topDomainTruststorePassword.toCharArray());
    }

    public static void generateKeystore(String keyPassword, String entryName, PrivateKey privateKey, List<X509Certificate> certificates, OutputStream stream) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        Certificate[] certificates1 = certificates.toArray(certificates.toArray(new X509Certificate[0]));
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null,null);
        keyStore.setKeyEntry(entryName, privateKey, keyPassword.toCharArray(), certificates1);

        keyStore.store(stream, keyPassword.toCharArray());
    }

    public static void saveCertChain(List<X509Certificate> certificateChain, Writer writer) throws IOException {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(writer)){
            for (X509Certificate cert : certificateChain) {
                pemWriter.writeObject(cert);
            }
        }
    }

    public static void saveCert(X509Certificate certificate, Writer writer) throws IOException {
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
            pemWriter.writeObject(certificate);
        }
    }


    public static CertificateAndCertificateChain signIntermediateCsr(X509Certificate caCert, List<X509Certificate> certChain,PrivateKey caPrivateKey, PKCS10CertificationRequest csr, SecureRandom secureRandom) throws NoSuchAlgorithmException, CertIOException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException, OperatorCreationException {
        JcaPKCS10CertificationRequest csrWrapper = new JcaPKCS10CertificationRequest(csr);
        PublicKey subjectPublicKey = csrWrapper.getPublicKey();
        X500Name issuerSubject = JcaX500NameUtil.getSubject(caCert);
        X500Name csrSubject = csr.getSubject();
        PublicKey caPublicKey = caCert.getPublicKey();

        X509Certificate certificate = signX509Certificate(csrSubject, subjectPublicKey, issuerSubject, caPublicKey, caPrivateKey, secureRandom);

        List<X509Certificate> newCertChain = new ArrayList<>();
        newCertChain.add(certificate);
        newCertChain.addAll(certChain);
        return new CertificateAndCertificateChain(certificate, newCertChain);
    }

    private static X509Certificate signX509Certificate(X500Name subjectName, PublicKey subjectPublicKey, X500Name issuerName, PublicKey issuerPublicKey, PrivateKey issuerPrivateKey, SecureRandom secureRandom) throws CertIOException, NoSuchAlgorithmException, OperatorCreationException, CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {
        JcaContentSignerBuilder signerBuilder = createContentSignerBuilder();
        BigInteger serialNumber = new BigInteger(Long.toString(secureRandom.nextLong()));
        Date startDate = new Date();
        Date toDate = Date.from(LocalDateTime.now().plusYears(1).atZone(ZoneId.systemDefault()).toInstant());

        JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                issuerName,
                serialNumber,
                startDate,
                toDate,
                subjectName,
                subjectPublicKey
        );
        certificateBuilder.addExtension(Extension.basicConstraints,true,new BasicConstraints(true));
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign);
        certificateBuilder.addExtension(Extension.keyUsage,true,keyUsage);
        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
        AuthorityKeyIdentifier authorityKeyIdentifier = extensionUtils.createAuthorityKeyIdentifier(issuerPublicKey);
        certificateBuilder.addExtension(Extension.authorityKeyIdentifier,false, authorityKeyIdentifier);
        SubjectKeyIdentifier subjectKeyIdentifier = extensionUtils.createSubjectKeyIdentifier(subjectPublicKey);
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier,false, subjectKeyIdentifier);

        ContentSigner signer = signerBuilder.build(issuerPrivateKey);


        X509CertificateHolder issuedCertHolder = certificateBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(issuedCertHolder);
        certificate.verify(issuerPublicKey);
        return certificate;
    }


    public static void saveKeyPair(KeyPair keyPair, Writer keyWriter) throws IOException {
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
        X500Name x500Name = new X500Name(
                String.format(
                        "CN=%s, O=Nordic Way, C=%s",
                        domainName,
                        owningCountry
                )

        );
        return createCertificateRequest(x500Name, keyPair);
    }

    private static PKCS10CertificationRequest createCertificateRequest(X500Name subject, KeyPair keyPair) throws OperatorCreationException {
        JcaPKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
        JcaContentSignerBuilder signBuilder = createContentSignerBuilder();
        ContentSigner signer = signBuilder.build(keyPair.getPrivate());
        return builder.build(signer);
    }

    private static JcaContentSignerBuilder createContentSignerBuilder() {
        return new JcaContentSignerBuilder("SHA512withRSA");
    }


    public static CertificateCertificateChainAndKeys generateTopCa(String commonName, String ownerCountry, SecureRandom secureRandom) throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, CertIOException, SignatureException, InvalidKeyException, NoSuchProviderException {
        if (ownerCountry == null) {
            ownerCountry = "NO";
        }
        KeyPair keyPair = generateKeyPair(4096);
        X500Name subject = new X500Name(
                String.format(
                        "CN=%s, O=Nordic Way, C=%s",
                        commonName,
                        ownerCountry
                )
        );
        PublicKey publicKey = keyPair.getPublic();

        X509Certificate cert = signX509Certificate(subject, publicKey, subject, publicKey, keyPair.getPrivate(),secureRandom);
        KeyPairAndCertificate details = new KeyPairAndCertificate(keyPair, cert);
        ArrayList<X509Certificate> certificates = new ArrayList<>();
        certificates.add(details.certificate());
        return new CertificateCertificateChainAndKeys(details.keyPair(), details.certificate(),certificates);
    }

    public static CertificateCertificateChainAndKeys generateIntermediateCA(String commonName, String country, List<X509Certificate> issuerCertChain, X509Certificate issuerCert, PrivateKey issuerKey, SecureRandom secureRandom) throws NoSuchAlgorithmException, OperatorCreationException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException, CertIOException {
        KeyPairAndCsr intermediateCsr = generateIntermediateKeypairAndCsr(commonName, country);
        CertificateAndCertificateChain intermediateCert = signIntermediateCsr(issuerCert, issuerCertChain,issuerKey, intermediateCsr.csr(), secureRandom);
        return new CertificateCertificateChainAndKeys(intermediateCsr.keyPair(),intermediateCert.certificate(),intermediateCert.chain());
    }

    public static X509Certificate loadSingleCertificate(Reader reader) throws IOException, CertificateException {
        PEMParser certificateParser = new PEMParser(reader);
        X509CertificateHolder certificateHolder = (X509CertificateHolder) certificateParser.readObject();
        return new JcaX509CertificateConverter().getCertificate(certificateHolder);
    }

    public static List<X509Certificate> loadCertificateChain(Reader reader) throws IOException, CertificateException {
        PEMParser parser = new PEMParser(reader);
        List<X509Certificate> certificates = new ArrayList<>();
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        Object o = parser.readObject();
        while (o  != null) {
            certificates.add(converter.getCertificate((X509CertificateHolder) o));
            o = parser.readObject();
        }
        return certificates;
    }

    public static PrivateKey loadPrivateKey(Reader reader) throws IOException {
        PEMParser keyParser = new PEMParser(reader);
        return new JcaPEMKeyConverter().getPrivateKey((PrivateKeyInfo) keyParser.readObject());

    }

    public static KeyPair loadKeyPair(Reader reader) throws IOException {
        PEMParser parser = new PEMParser(reader);
        return new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) parser.readObject());
    }

    public static List<CaResponse> readCaResponsesFromJson(Reader reader) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(
                CertificateCertificateChainAndKeys.class,
                new CertificateCertificateChainAndKeysDeserializer()
        );
        mapper.registerModule(module);
        return mapper.readerForListOf(CaResponse.class).readValue(reader);
    }

    public static void writeCaReponsesToJson(Writer writer, List<CaResponse> responses) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(
                CertificateCertificateChainAndKeys.class,
                new CertificateCertificateChainAndKeysSerializer()
        );
        mapper.registerModule(module);
        mapper.writerWithDefaultPrettyPrinter().writeValue(
                writer,
                responses
        );
    }


    public record KeyPairAndCertificate(KeyPair keyPair, X509Certificate certificate) {
    }

    private static KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.generateKeyPair();
    }


    public interface PasswordGenerator {
        String generatePassword();
    }
    
    public static class RandomPasswordGenerator implements PasswordGenerator {

        private final SecureRandom random;
        private final int length;

        public RandomPasswordGenerator(SecureRandom random, int length) {
            this.random = random;
            this.length = length;
        }

        @Override
        public String generatePassword() {
            StringBuilder builder = new StringBuilder();
            for (int i =  0; i < length; i++) {
                builder.append(allowedChars[random.nextInt(allowedChars.length)]);
            }
            return builder.toString();
        }
    }


    public record KeyPairAndCsr(KeyPair keyPair, PKCS10CertificationRequest csr) {
    }

    public record CertificateAndCertificateChain(X509Certificate certificate, List<X509Certificate> chain) {
    }

    public record CertificateCertificateChainAndKeys(KeyPair keyPair, X509Certificate certificate, List<X509Certificate> certificateChain) {
    }

    public static class CertificateCertificateChainAndKeysSerializer extends JsonSerializer<CertificateCertificateChainAndKeys> {

        @Override
        public void serialize(CertificateCertificateChainAndKeys value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            StringWriter keyWriter = new StringWriter();
            Base64.Encoder encoder = Base64.getEncoder();
            saveKeyPair(value.keyPair(), keyWriter);
            String keypairString = encoder.encodeToString(keyWriter.toString().getBytes());
            gen.writeStringField("keypair", keypairString);
            StringWriter certWriter = new StringWriter();
            saveCert(value.certificate(), certWriter);
            String certString = encoder.encodeToString(certWriter.toString().getBytes());
            gen.writeStringField("cert",certString);
            StringWriter certChainWriter = new StringWriter();
            saveCertChain(value.certificateChain(), certChainWriter);
            String certChainString = encoder.encodeToString(certChainWriter.toString().getBytes());
            gen.writeStringField("certChain",certChainString);
            gen.writeEndObject();
        }
    }

    public static class CertificateCertificateChainAndKeysDeserializer extends JsonDeserializer<CertificateCertificateChainAndKeys> {

        @Override
        public CertificateCertificateChainAndKeys deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Base64.Decoder decoder = Base64.getDecoder();
            JsonNode node = p.readValueAsTree();
            String encodedKeyPair = node.get("keypair").asText();
            String decodedKeyPair = new String(decoder.decode(encodedKeyPair));
            KeyPair keyPair = loadKeyPair(new StringReader(decodedKeyPair));
            String encodedCert = node.get("cert").asText();
            String decodedCert = new String(decoder.decode(encodedCert));
            X509Certificate certificate;
            try {
                certificate = loadSingleCertificate(new StringReader(decodedCert));
            } catch (CertificateException e) {
                throw new RuntimeException(e);
            }
            String encodedCertChain = node.get("certChain").asText();
            String decodedCertChain = new String(decoder.decode(encodedCertChain));
            List<X509Certificate> certificateChain;
            try {
                certificateChain = loadCertificateChain(new StringReader(decodedCertChain));
            } catch (CertificateException e) {
                throw new RuntimeException(e);
            }
            return new CertificateCertificateChainAndKeys(keyPair,certificate,certificateChain);
        }
    }
}
