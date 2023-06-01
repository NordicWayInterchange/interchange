package no.vegvesen.ixn.cert;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.file.Path;
import java.security.*;

public class CaGenerator {

    public static void generateRootCA_BC() {
        for (Provider provider : Security.getProviders()) {
            System.out.println(provider.getName());
        }
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("SHA256withRSA",new BouncyCastleProvider());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
