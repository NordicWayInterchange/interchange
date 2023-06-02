package no.vegvesen.ixn.cert;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.file.Path;
import java.security.*;

public class CaGenerator {

    public static void generateRootCA_BC() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA","BC");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

}
