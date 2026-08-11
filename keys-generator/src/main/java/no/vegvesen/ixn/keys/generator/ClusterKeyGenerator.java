package no.vegvesen.ixn.keys.generator;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.module.SimpleModule;
import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.cert.CsrGenerator;
import no.vegvesen.ixn.cert.KeyPairAndCsr;
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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class ClusterKeyGenerator {


    /**
     * Traverses the certificate tree, generating certificates and keys for all the entities in the tree.
     * Useful for generating keys and certs for tests involving several nodes or interchanges.
     * @param caRequest The top CA, with sub CA's with associated host and client certs
     * @return
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws OperatorCreationException
     * @throws InvalidKeyException
     * @throws NoSuchProviderException
     * @throws CertIOException
     */
    public static CaResponse generate(CARequest caRequest) throws CertificateException, NoSuchAlgorithmException, SignatureException, OperatorCreationException, InvalidKeyException, NoSuchProviderException, CertIOException {
        SecureRandom random = new SecureRandom();
        CertificateCertificateChainAndKeys topCa = generateTopCa(caRequest.name(), caRequest.country(), random);
        List<HostResponse> hostResponses = getHostResponses(caRequest.hostRequests(), topCa, random);
        List<ClientResponse> clientResponses = getClientResponses(caRequest.clientRequests(), topCa);

        return generateSubCaResponses(caRequest, random, topCa, hostResponses, clientResponses);

    }

    private static CaResponse generate(CARequest caRequest, CertificateCertificateChainAndKeys parentCa, SecureRandom random) throws CertificateException, NoSuchAlgorithmException, SignatureException, OperatorCreationException, InvalidKeyException, NoSuchProviderException, CertIOException {
        CertificateCertificateChainAndKeys intermediateCa = generateIntermediateCA(caRequest.name(), caRequest.country(), parentCa.certificateChain(), parentCa.certificate(), parentCa.keyPair().getPrivate(), random);
        List<ClientResponse> clientResponses = getClientResponses(caRequest.clientRequests(), intermediateCa);
        List<HostResponse> hostResponses = getHostResponses(caRequest.hostRequests(),intermediateCa,random);
        return generateSubCaResponses(caRequest, random, intermediateCa, hostResponses, clientResponses);
    }

    private static CaResponse generateSubCaResponses(CARequest caRequest, SecureRandom random, CertificateCertificateChainAndKeys ca, List<HostResponse> hostResponses, List<ClientResponse> clientResponses) throws CertificateException, NoSuchAlgorithmException, SignatureException, OperatorCreationException, InvalidKeyException, NoSuchProviderException, CertIOException {
        List<CaResponse> responses = new ArrayList<>();
        for (CARequest request : caRequest.subCaRequests()) {
            CaResponse response = generate(request, ca, random);
            responses.add(response);
        }
        return new CaResponse(ca, caRequest.name(), hostResponses, clientResponses,responses);
    }

    public static void storePems(CaResponse response, Path basePath) throws IOException {
        saveKeyPair(response.details().keyPair(), Files.newBufferedWriter(basePath.resolve(response.name() + ".pem")));
        for (HostResponse hostResponse : response.hostResponses()) {
           saveKeyPair(hostResponse.keyDetails().keyPair(), Files.newBufferedWriter(basePath.resolve(hostResponse.host() + ".pem")));
        }
        for (ClientResponse clientResponse : response.clientResponses()) {
            saveKeyPair(clientResponse.clientDetails().keyPair(), Files.newBufferedWriter(basePath.resolve(clientResponse.name() + ".pem")));
        }
        for (CaResponse caResponse : response.caResponses()) {
            storePems(caResponse, basePath);
        }
    }

    /**
     * Makes a keystore and truststore for the CA (truststore containing the CA cert, plus cert without chain for the CA),
     * and keystores for each host and client in the chain
     *
     */
    public static CaStores store(CaResponse response, Path basePath, PasswordGenerator passwordGenerator) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        CaStore caStore = trustStoreForCa(response, basePath, passwordGenerator);
        saveCert(response.details().certificate(),  Files.newBufferedWriter(basePath.resolve(response.name() + ".crt.pem")));
        List<HostStore> hostStores = storeHostResponses(basePath, passwordGenerator, response.hostResponses());
        List<ClientStore> clientStores = storeClientStores(basePath, passwordGenerator, response.clientResponses());
        List<CaStores> subCaStores = new ArrayList<>();
        for (CaResponse subResponses : response.caResponses()) {
            CaStores store = store(subResponses, basePath, passwordGenerator);
            subCaStores.add(store);
        }
        return new CaStores(response.name(), caStore, hostStores, clientStores, subCaStores);
    }

    private static CaStore trustStoreForCa(CaResponse response, Path basePath, PasswordGenerator passwordGenerator) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        String truststorePassword = passwordGenerator.generatePassword();
        Files.writeString(basePath.resolve(response.name() + ".jks.txt"),truststorePassword);
        Path truststorePath = basePath.resolve(response.name() + ".jks");
        try (OutputStream outputStream = Files.newOutputStream(truststorePath)) {
            makeTrustStore(
                    truststorePassword,
                    outputStream,
                    response.details().certificate(),
                    "myKey");
        }
        String keystorePassword = passwordGenerator.generatePassword();
        Files.writeString(basePath.resolve(response.name() + ".p12.txt"),keystorePassword);
        Path keystorePath = basePath.resolve(response.name() + ".p12");
        try (OutputStream outputStream = Files.newOutputStream(keystorePath)) {
            makeKeystore(
                    response.name(),
                    keystorePassword,
                    outputStream,
                    response.details().certificateChain(),
                    response.details().keyPair.getPrivate()
            );

        }
        return new CaStore(response.name(), truststorePath, truststorePassword,keystorePath,keystorePassword);
    }

    public static List<HostStore> storeHostResponses(Path basePath, PasswordGenerator randomPasswordGenerator, List<HostResponse> hostResponses) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        List<HostStore> hostStores = new ArrayList<>();
        for (HostResponse hostResponse : hostResponses) {
            String hostPassword = randomPasswordGenerator.generatePassword();
            Files.writeString(basePath.resolve(hostResponse.host() + ".txt"),hostPassword);
            Path outputPath = basePath.resolve(hostResponse.host() + ".p12");
            makeKeystore(hostResponse.host(), hostPassword, Files.newOutputStream(outputPath), hostResponse.keyDetails().certificateChain(), hostResponse.keyDetails().keyPair().getPrivate());
            hostStores.add(new HostStore(hostResponse.host(),outputPath,hostPassword));
        }
        return hostStores;
    }
    
    private static List<ClientStore> storeClientStores(Path basePath, PasswordGenerator randomPasswordGenerator, List<ClientResponse> clientResponses) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        List<ClientStore> clientStores = new ArrayList<>();
        for (ClientResponse clientResponse : clientResponses) {
            String clientPassword = randomPasswordGenerator.generatePassword();
            String name = clientResponse.name();
            Files.writeString(basePath.resolve(name + ".txt"),clientPassword);
            Path path = basePath.resolve(name + ".p12");
            makeKeystore(name, clientPassword, Files.newOutputStream(path), clientResponse.clientDetails().certificateChain(), clientResponse.clientDetails().keyPair().getPrivate());
            clientStores.add(new ClientStore(name, path,clientPassword));
        }
        return clientStores;
    }

    public static HostStore getHostStore(String hostname, Stream<HostStore> stream) {
        return stream.filter(h -> h.hostname().equals(hostname)).findAny().orElseThrow(() -> new RuntimeException("No store found for hostname: " + hostname));
    }

    public static ClientStore getClientStore(String serviceProviderName, Stream<ClientStore> stream) {
        return stream.filter(c -> c.clientName().equals(serviceProviderName)).findAny().orElseThrow(() -> new RuntimeException("No client store found for " + serviceProviderName));
    }


    public record CaStores(String name, CaStore trustStore, List<HostStore> hostStores, List<ClientStore> clientStores, List<CaStores> subCaStores) {}


    /**
     * A CaResponse gives
     *  <li>a truststore, containing the certificate of the CA</li>
     *  <li>a keystore containing the keys, and a cert containing the entire cert chain</li>
     */
    public record CaStore(String name, Path truststoreName, String truststorePassword, Path keystoreName, String keystorePassword) {}

    //a HostResponse or a ClientResponse gives a keystore
    public record HostStore(String hostname, Path path, String password) {}

    public record ClientStore(String clientName, Path path, String password) {}
    
    
    
    
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

    public static CertificateCertificateChainAndKeys generateSPKeys(String commonName, String spCountry, String spEmail, X509Certificate issuerCertificate, PrivateKey issuerPrivateKey, List<X509Certificate> issuerCertChain) throws NoSuchAlgorithmException, OperatorCreationException, CertIOException, CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {
        X500Name x500Name = new X500Name(
                String.format(
                        "emailAddress=%s, CN=%s, O=Nordic Way, C=%s",
                        spEmail,
                        commonName,
                        spCountry
                )
        );
        KeyPairAndCsr spCsr = new CsrGenerator("RSA", 2048, "SHA512withRSA")
                .generateKeyPairAndCsr(x500Name);
        CertSigner certSigner = new CertSigner(issuerPrivateKey, issuerCertificate, issuerCertChain);
        List<X509Certificate> newCertChain = certSigner.sign(spCsr.csr(), commonName);
        return new CertificateCertificateChainAndKeys(spCsr.keyPair(), newCertChain.get(0),newCertChain);
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

    public static void makeKeystore(String name, String password, OutputStream outputStream, List<X509Certificate> certificateChain, PrivateKey aPrivate) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keyStore = newKeyStore(name, password, certificateChain, aPrivate);
        keyStore.store(outputStream,password.toCharArray());
    }

    public static KeyStore newKeyStore(String name, String password, List<X509Certificate> certificateChain, PrivateKey aPrivate) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        X509Certificate[] certificates = certificateChain.toArray(new X509Certificate[0]);
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null,null);
        keyStore.setKeyEntry(name, aPrivate, password.toCharArray(),certificates);
        return keyStore;
    }

    public static void makeTrustStore(String truststorePassword, OutputStream outputStream, X509Certificate certificate, String myKey) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore trustStore = newTrustStore(certificate, myKey);
        trustStore.store(outputStream,truststorePassword.toCharArray());
    }

    public static KeyStore newTrustStore(X509Certificate certificate, String myKey) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null,null);
        trustStore.setCertificateEntry(myKey, certificate);
        return trustStore;
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

        X509Certificate certificate = signX509CaCertificate(csrSubject, subjectPublicKey, issuerSubject, caPublicKey, caPrivateKey, secureRandom);

        List<X509Certificate> newCertChain = new ArrayList<>();
        newCertChain.add(certificate);
        newCertChain.addAll(certChain);
        return new CertificateAndCertificateChain(certificate, newCertChain);
    }

    private static X509Certificate signX509CaCertificate(X500Name subjectName, PublicKey subjectPublicKey, X500Name issuerName, PublicKey issuerPublicKey, PrivateKey issuerPrivateKey, SecureRandom secureRandom) throws CertIOException, NoSuchAlgorithmException, OperatorCreationException, CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {
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

        //RFC 5280: Trust anchor Basic contraints must assert CA
        BasicConstraints basicContraints = new BasicConstraints(true);
        //RFC 5280: Trust anchor must contain basic constraints section marked critical
        certificateBuilder.addExtension(Extension.basicConstraints,true, basicContraints);

        //RFC 5280: Trust anchor keyUsage must have keyCertSign, cRLsign
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign);
        //RFC 5280: Trust anchor must contain key usage section marked critical
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

    public static void saveKey(PrivateKey key, Writer keyWriter) throws IOException {
        JcaPEMWriter pemWriter;
        pemWriter = new JcaPEMWriter(keyWriter);
        pemWriter.writeObject(key);
        pemWriter.close();
    }

    public static void saveCSR(PKCS10CertificationRequest csr, Writer keyWriter) throws IOException {
        JcaPEMWriter pemWriter;
        pemWriter = new JcaPEMWriter(keyWriter);
        pemWriter.writeObject(csr);
        pemWriter.close();
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

        X509Certificate cert = signX509CaCertificate(subject, publicKey, subject, publicKey, keyPair.getPrivate(),secureRandom);
        KeyPairAndCertificate details = new KeyPairAndCertificate(keyPair, cert);
        ArrayList<X509Certificate> certificates = new ArrayList<>();
        certificates.add(details.certificate());
        return new CertificateCertificateChainAndKeys(details.keyPair(), details.certificate(),certificates);
    }

    public static CertificateCertificateChainAndKeys generateIntermediateCA(String commonName, String country, List<X509Certificate> issuerCertChain, X509Certificate issuerCert, PrivateKey issuerKey, SecureRandom secureRandom) throws NoSuchAlgorithmException, OperatorCreationException, CertificateException, SignatureException, InvalidKeyException, NoSuchProviderException, CertIOException {
        CsrGenerator generator = new CsrGenerator("RSA",4096,"SHA512withRSA");
        if (country == null) {
            country = "NO";
        }
        X500Name x500Name = new X500Name(
                String.format(
                        "CN=%s, O=Nordic Way, C=%s",
                        commonName,
                        country
                )

        );
        KeyPairAndCsr intermediateCsr = generator.generateKeyPairAndCsr(x500Name);
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

    public static PrivateKey safeLoadPrivateKey(Reader reader) throws IOException {
        PEMParser keyParser = new PEMParser(reader);
        Object obj = keyParser.readObject();
        PrivateKey privateKey = null;
        switch (obj) {
            case PEMKeyPair keyPair -> privateKey = new JcaPEMKeyConverter().getKeyPair(keyPair).getPrivate();
            case PrivateKeyInfo privateKeyInfo -> privateKey = new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
            default -> throw new RuntimeException("Cannot determine key type");
        }
        return privateKey;

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




    public record CertificateAndCertificateChain(X509Certificate certificate, List<X509Certificate> chain) {

    }

    public record CertificateCertificateChainAndKeys(KeyPair keyPair, X509Certificate certificate, List<X509Certificate> certificateChain) {
    }

    public static class CertificateCertificateChainAndKeysSerializer extends ValueSerializer<CertificateCertificateChainAndKeys> {

        @Override
        public void serialize(CertificateCertificateChainAndKeys value, JsonGenerator gen, SerializationContext context) {
            gen.writeStartObject();
            StringWriter keyWriter = new StringWriter();
            Base64.Encoder encoder = Base64.getEncoder();
            saveKeyPair(value.keyPair(), keyWriter);
            String keypairString = encoder.encodeToString(keyWriter.toString().getBytes());
            gen.writeStringProperty("keypair", keypairString);
            StringWriter certWriter = new StringWriter();
            saveCert(value.certificate(), certWriter);
            String certString = encoder.encodeToString(certWriter.toString().getBytes());
            gen.writeStringProperty("cert",certString);
            StringWriter certChainWriter = new StringWriter();
            saveCertChain(value.certificateChain(), certChainWriter);
            String certChainString = encoder.encodeToString(certChainWriter.toString().getBytes());
            gen.writeStringProperty("certChain",certChainString);
            gen.writeEndObject();
        }
    }

    public static class CertificateCertificateChainAndKeysDeserializer extends ValueDeserializer<CertificateCertificateChainAndKeys> {

        @Override
        public CertificateCertificateChainAndKeys deserialize(JsonParser p, DeserializationContext ctxt) {
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
