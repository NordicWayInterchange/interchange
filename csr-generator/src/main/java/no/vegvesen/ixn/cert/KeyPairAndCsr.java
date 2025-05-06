package no.vegvesen.ixn.cert;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;

public record KeyPairAndCsr(KeyPair keyPair, PKCS10CertificationRequest csr) {

    public String csrToPem() throws IOException {
        StringWriter writer = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        pemWriter.writeObject(csr);
        pemWriter.close();
        return writer.toString();

    }

    public String privateKeyToPem() throws IOException {
        StringWriter writer = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        pemWriter.writeObject(keyPair.getPrivate());
        pemWriter.close();
        return writer.toString();
    }
}
