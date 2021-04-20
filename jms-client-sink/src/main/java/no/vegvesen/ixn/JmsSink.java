package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.net.ssl.SSLContext;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        if (properties.getMessageFileName().isEmpty()) {
            Sink sink = new Sink(url, receiveQueue, sslContext);
            System.out.println(String.format("Listening for messages from queue [%s] on server [%s]", receiveQueue, url));
            sink.start();
        } else {
            int expecedMessages = 1;
            int maxSecondsPerMessage = 10;
            int maxWaitSeconds = expecedMessages * maxSecondsPerMessage;
            CountDownLatch latch = new CountDownLatch(expecedMessages);
            List<String> messages = Collections.synchronizedList(new ArrayList<>());
            String fileName = properties.getMessageFileName();
            try (Sink sink = new Sink(url, receiveQueue, sslContext)) {
                sink.startWithMessageListener(message -> {
                    try {
                        if (message instanceof JmsTextMessage) {
                            messages.add(message.getBody(String.class));
                        } else {
                            messages.add("Binary message");
                        }
                    } catch (JMSException e) {
                        messages.add("Exception " + e);
                    } finally {
                        latch.countDown();
                    }
                });
                latch.await(maxWaitSeconds, TimeUnit.SECONDS);
            }
            try (PrintWriter out = new PrintWriter(Files.newOutputStream(Paths.get(fileName)))) {
                for (String message : messages) {
                    out.println(message);
                }
            }
        }
    }
}
