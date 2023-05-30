package no.vegvesen.ixn.cert;

import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class CaGenerator {

    public static void generateRootCA_BC() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("SHA512WITHRSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
