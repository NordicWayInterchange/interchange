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
public class JmsSink implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(JmsSink.class);
    }

    @Autowired
    private JmsSinkProperties properties;

    @Override
    public void run(String... args) throws Exception {

        KeystoreDetails keystoreDetails = new KeystoreDetails(properties.getKeystorePath(),
                properties.getKeystorePass(),
                KeystoreType.PKCS12, properties.getKeyPass());
        KeystoreDetails trustStoreDetails = new KeystoreDetails(properties.getTrustStorePath(),
                properties.getKeystorePass(),KeystoreType.JKS);

        SSLContext sslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);

        String url = properties.getUrl();
        String receiveQueue = properties.getReceiveQueue();
        Sink sink = new Sink(url,receiveQueue,sslContext);
        System.out.println(String.format("Listening for messages from queue [%s] on server [%s]", receiveQueue, url));
        sink.start();
    }
}
