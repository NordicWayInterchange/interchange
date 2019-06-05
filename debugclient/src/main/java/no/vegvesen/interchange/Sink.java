package no.vegvesen.interchange;

import no.vegvesen.ixn.IxnContext;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.jms.*;
import javax.net.ssl.SSLContext;

public class Sink implements MessageListener {
    public static void main(String[] args) throws Exception {

        String url = "amqps://remote:5601";
        String receiveQueue = "fedTest";
        String keystorePath = "/interchange/tmp/keys/remote.p12";
        String keystorePassword = "password";
        String keyPassword = "password";
        String trustStorePath = "/interchange/tmp/keys/truststore.jks";
        String truststorePassword = "password";

        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12, keyPassword);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                truststorePassword,KeystoreType.JKS);

        SSLContext sslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);


        IxnContext context = new IxnContext(url, null, receiveQueue);
        Connection connection = context.createConnection(sslContext);
        Destination destination = context.getReceiveQueue();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(new Sink());
    }

    @Override
    public void onMessage(Message message) {
        try {
            message.acknowledge();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Message received");
        try {
            System.out.println(((TextMessage)message).getText());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

    }
}
