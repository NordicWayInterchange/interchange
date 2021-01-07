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
public class JmsImageSource implements CommandLineRunner {

    /**
     * A message source. Sends a single message, and exits.
     */
    public static void main(String[] args) {
        SpringApplication.run(JmsImageSource.class);
    }

    @Autowired
    private JmsImageSourceProperties properties;

    @Override
    public void run(String... args) throws Exception {
        KeystoreDetails keystoreDetails = new KeystoreDetails(properties.getKeystorePath(),
                properties.getKeystorePass(),
                KeystoreType.PKCS12, properties.getKeyPass());
        KeystoreDetails trustStoreDetails = new KeystoreDetails(properties.getTrustStorepath(),
                properties.getTrustStorepass(),KeystoreType.JKS);
        SSLContext sslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
        try(ImageSource s = new ImageSource(properties.getUrl(),properties.getSendQueue(),sslContext)) {
            s.start();
            s.sendByteMessageWithImage( "NO", ",01230122", "cabin_view.jpg");
        }
    }
}
