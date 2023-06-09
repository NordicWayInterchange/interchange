package no.vegvesen.ixn.cert;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class CertSigner {
    private final PrivateKey privateKey;
    private final X509Certificate intermediateCert;
    private final SecureRandom secureRandom;
    private X500Name intermediateSubject;

    public CertSigner(String caCert, String key) throws IOException, CertificateException {
        secureRandom = new SecureRandom();
        privateKey = getPrivateKey(key);
        X509CertificateHolder certificateHolder = parseCertPem(caCert);
        intermediateSubject = certificateHolder.getSubject();
        intermediateCert = convertToX509Certificate(certificateHolder);
    }

    public String sign(String csrAsString,String cn) throws IOException, OperatorCreationException, CertificateException, NoSuchAlgorithmException {

        PKCS10CertificationRequest csr = getPkcs10CertificationRequest(csrAsString);

        SubjectPublicKeyInfo csrSubjectPublicKeyInfo = csr.getSubjectPublicKeyInfo();
        //Note: We do not save the serial number of the cert at this stage, this should probably be done.
        //But could probably just read it later from the cert.
        BigInteger serialNumber = new BigInteger(Long.toString(secureRandom.nextLong()));
        Date startDate = new Date();
        X500Name csrSubject = csr.getSubject();
        RDN csrCnRdn = csrSubject.getRDNs(BCStyle.CN)[0];


        //TODO check the csrCn is correct. (Also other tings? Like org?)
        String csrCn = IETFUtils.valueToString(csrCnRdn.getFirst().getValue());
        System.out.println(csrCn);

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                intermediateSubject,
                serialNumber,
                startDate,
                Date.from(LocalDateTime.now().plus(1, ChronoUnit.YEARS).atZone(ZoneId.systemDefault()).toInstant()),
                csrSubject,
                csrSubjectPublicKeyInfo
        );
        /*
        Extensions: Note that we specify this, rather than copy from csr. Might this be a problem?
         */
        //NOTE basic constraints are not critical. Should they be?
        certificateBuilder.addExtension(Extension.basicConstraints,false,new BasicConstraints(false));

        //NOTE this only gives the keyId for the key identifier. Could also use the cert,
        //which gives keyid, dirname (subject) and serial number of signee
        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
        AuthorityKeyIdentifier authorityKeyIdentifier = extensionUtils.createAuthorityKeyIdentifier(intermediateCert.getPublicKey());
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
        StringWriter certWriter = new StringWriter();
        JcaPEMWriter writer = new JcaPEMWriter(certWriter);
        writer.writeObject(certificate);
        writer.close();
        return certWriter.toString();
    }

    private X509Certificate convertToX509Certificate(X509CertificateHolder certificateHolder) throws CertificateException {
        JcaX509CertificateConverter jcaX509CertificateConverter = new JcaX509CertificateConverter();
        return jcaX509CertificateConverter.getCertificate(certificateHolder);
    }

    private X509CertificateHolder parseCertPem(String caCert) throws IOException {
        final X509CertificateHolder intermediateCertHolder;
        PEMParser certParser = new PEMParser(new StringReader(caCert));
        intermediateCertHolder = (X509CertificateHolder) certParser.readObject();
        return intermediateCertHolder;
    }

    private PKCS10CertificationRequest getPkcs10CertificationRequest(String csrAsString) throws IOException {
        PEMParser csrParser = new PEMParser(new StringReader(csrAsString));
        PKCS10CertificationRequest csr = (PKCS10CertificationRequest) csrParser.readObject();
        return csr;
    }

    private PrivateKey getPrivateKey(String key) throws IOException {
        final PrivateKey privateKey;
        PEMParser keyParser = new PEMParser(new StringReader(key));
        PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(keyParser.readObject());
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        privateKey = converter.getPrivateKey(privateKeyInfo);
        return privateKey;
    }
}
