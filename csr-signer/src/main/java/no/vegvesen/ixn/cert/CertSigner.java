package no.vegvesen.ixn.cert;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX500NameUtil;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class CertSigner {
    private final PrivateKey privateKey;
    private final SecureRandom secureRandom;
    private final X500Name issuerSubject;
    private final X509Certificate issuerCertificate;
    private final List<X509Certificate> certificateChain;


    public CertSigner(PrivateKey issuerPrivateKey,
                      X509Certificate issuerCertificate,
                      X509Certificate caCertificate) {
        this.secureRandom = new SecureRandom();
        this.privateKey = issuerPrivateKey;
        this.issuerCertificate = issuerCertificate;
        this.issuerSubject = JcaX500NameUtil.getSubject(issuerCertificate);
        this.certificateChain = Arrays.asList(issuerCertificate, caCertificate);
    }

    /**
     *
     * @param issuerPrivateKey Private key of the issuer
     * @param issuerCertificate Certificate of the issuer
     * @param certChain Complete certificate chain, including the issuer certificate
     */
    public CertSigner(PrivateKey issuerPrivateKey,
                      X509Certificate issuerCertificate,
                      List<X509Certificate> certChain) {
        this.secureRandom = new SecureRandom();
        this.privateKey = issuerPrivateKey;
        this.issuerCertificate = issuerCertificate;
        this.issuerSubject = JcaX500NameUtil.getSubject(issuerCertificate);
        this.certificateChain = certChain;

    }

    public static X509Certificate getCertificate(KeyStore keyStore, String keyAlias) throws KeyStoreException {
        return (X509Certificate) keyStore.getCertificate(keyAlias);
    }

    public List<String> sign(String csrAsString,String cn) throws IOException, OperatorCreationException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {

        PKCS10CertificationRequest csr = getPkcs10CertificationRequest(csrAsString);
        List<X509Certificate> certificates = sign(csr, cn);
        return certificatesToString(certificates);
    }

    public List<X509Certificate> sign(PKCS10CertificationRequest csr, String cn) throws CertIOException, NoSuchAlgorithmException, OperatorCreationException, CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {
        SubjectPublicKeyInfo csrSubjectPublicKeyInfo = csr.getSubjectPublicKeyInfo();
        //Note: We do not save the serial number of the cert at this stage, this should probably be done.
        //But could probably just read it later from the cert.
        BigInteger serialNumber = new BigInteger(Long.toString(secureRandom.nextLong()));
        Date startDate = new Date();
        Date toDate = Date.from(LocalDateTime.now().plusYears(1).atZone(ZoneId.systemDefault()).toInstant());
        X500Name csrSubject = csr.getSubject();
        if (! cn.equals(getCN(csrSubject))) {
            throw new IllegalSubjectException();
        }

        JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                issuerSubject,
                serialNumber,
                startDate,
                toDate,
                csrSubject,
                csrSubjectPublicKeyInfo
        );
        //NOTE basic constraints are not critical. Should they be?
        certificateBuilder.addExtension(Extension.basicConstraints,false,new BasicConstraints(false));

        //NOTE this only gives the keyId for the key identifier. Could also use the cert,
        //which gives keyid, dirname (subject) and serial number of signee
        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
        AuthorityKeyIdentifier authorityKeyIdentifier = extensionUtils.createAuthorityKeyIdentifier(issuerCertificate.getPublicKey());
        certificateBuilder.addExtension(Extension.authorityKeyIdentifier,false, authorityKeyIdentifier);
        /*
            X509v3 Key Usage: critical
                Digital Signature, Non Repudiation, Key Encipherment

                Obtained by ORing the ints together.
         */
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation | KeyUsage.keyEncipherment);
        certificateBuilder.addExtension(Extension.keyUsage,true,keyUsage);

        KeyPurposeId keyPurpose = KeyPurposeId.id_kp_clientAuth;
        ExtendedKeyUsage extendedKeyUsage = new ExtendedKeyUsage(keyPurpose);
        certificateBuilder.addExtension(Extension.extendedKeyUsage,false,extendedKeyUsage);

        SubjectKeyIdentifier subjectKeyIdentifier = extensionUtils.createSubjectKeyIdentifier(csrSubjectPublicKeyInfo);
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier,false, subjectKeyIdentifier);

        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA512withRSA");

        ContentSigner signer = signerBuilder.build(privateKey);


        X509CertificateHolder issuedCertHolder = certificateBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(issuedCertHolder);
        certificate.verify(issuerCertificate.getPublicKey());
        ArrayList<X509Certificate> certs = new ArrayList<>();
        certs.add(certificate);
        certs.addAll(certificateChain);
        return certs;
    }

    public static String getCN(X500Name csrSubject) {
        RDN csrCnRdn = csrSubject.getRDNs(BCStyle.CN)[0];
        if (csrCnRdn.isMultiValued()) {
            for (AttributeTypeAndValue typeAndValue : csrCnRdn.getTypesAndValues()) {
                ASN1ObjectIdentifier type = typeAndValue.getType();
                if (BCStyle.CN.equals(type)) {
                    return IETFUtils.valueToString(typeAndValue.getValue());
                }
            }
            throw new RuntimeException("Could not find CN for Subject");
        } else {
            //TODO check the csrCn is correct. (Also other tings? Like org?)
            return IETFUtils.valueToString(csrCnRdn.getFirst().getValue());
        }
    }


    public static List<String> certificatesToString(List<X509Certificate> certificates) throws IOException {
        List<String> pemList = new ArrayList<>();
        for (X509Certificate certificate : certificates) {
            pemList.add(certificateAsString(certificate));
        }
        return pemList;
    }

    public static String certificateAsString(X509Certificate certificate) throws IOException {
        StringWriter certWriter = new StringWriter();
        JcaPEMWriter writer = new JcaPEMWriter(certWriter);
        writer.writeObject(certificate);
        writer.close();
        return certWriter.toString();
    }

    public static PKCS10CertificationRequest getPkcs10CertificationRequest(String csrAsString) throws IOException {
        PEMParser csrParser = new PEMParser(new StringReader(csrAsString));
        return (PKCS10CertificationRequest) csrParser.readObject();
    }


    public static KeyStore loadKeyStore(String keystoreLocation, String keyStorePassword, String storeType) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(storeType);
        keyStore.load(new FileInputStream(keystoreLocation),keyStorePassword.toCharArray());
        return keyStore;
    }

    public static PrivateKey getKey(KeyStore keyStore, String keyAlias, String keyStorePassword) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return (PrivateKey) keyStore.getKey(keyAlias, keyStorePassword.toCharArray());
    }

}
