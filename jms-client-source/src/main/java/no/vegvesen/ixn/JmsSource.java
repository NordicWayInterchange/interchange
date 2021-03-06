package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.SSLContext;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn")
public class JmsSource implements CommandLineRunner {

    /**
     * A message source. Sends a single message, and exits.
     */
    public static void main(String[] args) {
        SpringApplication.run(JmsSource.class);
    }

    @Autowired
    private JmsSourceProperties properties;

    @Override
    public void run(String... args) throws Exception {
        KeystoreDetails keystoreDetails = new KeystoreDetails(properties.getKeystorePath(),
                properties.getKeystorePass(),
                KeystoreType.PKCS12, properties.getKeyPass());
        KeystoreDetails trustStoreDetails = new KeystoreDetails(properties.getTrustStorepath(),
                properties.getTrustStorepass(),KeystoreType.JKS);
        SSLContext sslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
        try(Source s = new Source(properties.getUrl(),properties.getSendQueue(),sslContext)) {
            s.start();
            s.send("Dette er en test, FISK!", "NO", ",01220123");
        }
    }
}
