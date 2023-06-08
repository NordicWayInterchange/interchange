package no.vegvesen.ixn.cert;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
//import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class CertSigner {
    private final PrivateKey privateKey;
    private final X509CertificateHolder certificateHolder;

    public CertSigner(String caCert, String key) throws IOException {
        PEMParser keyParser = new PEMParser(new StringReader(key));
        PEMParser certParser = new PEMParser(new StringReader(caCert));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(keyParser.readObject());
        privateKey = converter.getPrivateKey(privateKeyInfo);
        certificateHolder = (X509CertificateHolder) certParser.readObject();
    }

    public String sign(String csrAsString,String cn) throws IOException, OperatorCreationException, CertificateException {


        PEMParser csrParser = new PEMParser(new StringReader(csrAsString));
        PKCS10CertificationRequest csr = (PKCS10CertificationRequest) csrParser.readObject();
        JcaPEMKeyConverter pemKeyConverter = new JcaPEMKeyConverter();
        PublicKey csrPublicKey = pemKeyConverter.getPublicKey(csr.getSubjectPublicKeyInfo());
        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                new X500Name(String.format("CN=%s",cn)),
                new BigInteger("1"),
                new Date(),
                Date.from(LocalDateTime.now().plus(1, ChronoUnit.YEARS).atZone(ZoneId.systemDefault()).toInstant()),
                csr.getSubject(),
                csr.getSubjectPublicKeyInfo()
        );
        //ASN1ObjectIdentifier basicConstraints = Extension.basicConstraints;
        //new BasicConstraints(false);
        //TODO critical??
        certificateBuilder.addExtension(Extension.basicConstraints,false,new BasicConstraints(false));
        /*
            X509v3 Key Usage: critical
                Digital Signature, Non Repudiation, Key Encipherment

                Obtained by ORing the ints together.
         */
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation);
        certificateBuilder.addExtension(Extension.keyUsage,true,keyUsage);


        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256withRSA");

        ContentSigner signer = signerBuilder.build(privateKey);


        X509CertificateHolder issuedCertHolder = certificateBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(issuedCertHolder);
        StringWriter certWriter = new StringWriter();
        JcaPEMWriter writer = new JcaPEMWriter(certWriter);
        writer.writeObject(certificate);
        writer.close();
        return certWriter.toString();
    }
}
