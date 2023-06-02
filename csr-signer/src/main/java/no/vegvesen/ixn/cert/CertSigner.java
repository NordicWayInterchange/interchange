package no.vegvesen.ixn.cert;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
//import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class CertSigner {
    private final String caCert;
    private final String key;

    public CertSigner(String caCert, String key) {

        this.caCert = caCert;
        this.key = key;
    }

    public String sign(String csrAsString,String cn) throws IOException {
        PEMParser pemParser = new PEMParser(new StringReader(caCert));
        Object result = pemParser.readObject();
        System.out.println(result.getClass().getName());

        X509CertificateHolder certificateHolder = (X509CertificateHolder)result;

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                new X500Name(String.format("CN=%s",cn)),
                new BigInteger("1"),
                new Date(),
                Date.from(LocalDateTime.now().plus(1, ChronoUnit.YEARS).atZone(ZoneId.systemDefault()).toInstant()),
                certificateHolder.getSubject(),
                certificateHolder.getSubjectPublicKeyInfo()
        );

        PEMParser keyParser = new PEMParser(new StringReader(key));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(keyParser.readObject());
        PrivateKey privateKey = converter.getPrivateKey(privateKeyInfo);

        //X509CertificateHolder issuedCertHolder = certificateBuilder.build()
        return null;
    }
}
