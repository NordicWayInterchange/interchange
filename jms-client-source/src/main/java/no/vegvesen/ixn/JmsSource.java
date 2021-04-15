package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.File;

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

        if(properties.getMessageFileName().isEmpty()) {
            try (Source s = new Source(properties.getUrl(), properties.getSendQueue(), sslContext)) {
                s.start();
                s.send("Dette er en test, FISK!", "NO", ",01220123");
            }
        } else {
            File file = new File(properties.getMessageFileName());
            ObjectMapper mapper = new ObjectMapper();
            MessageListApi messages = mapper.readValue(file, MessageListApi.class);
            sendTextMessage(messages, sslContext);
        }
    }

    public void sendTextMessage(MessageListApi messages, SSLContext sslContext) {
        try{Source s = new Source(properties.getUrl(), properties.getSendQueue(), sslContext);
            s.start();
            for (MessageApi message : messages.getMessages()) {
                s.sendDatexMessageFromFile(message.getMessageText(),
                        message.getQuadTreeTiles(),
                        message.getOriginatingCountry(),
                        message.getPublisherId(),
                        message.getLatitude(),
                        message.getLongitude(),
                        message.getProtocolVersion(),
                        message.getServiceType(),
                        message.getPublicationType(),
                        message.getPublicationSubType());
            }
            s.close();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
