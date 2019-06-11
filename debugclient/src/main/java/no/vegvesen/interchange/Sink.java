package no.vegvesen.interchange;

import no.vegvesen.ixn.IxnContext;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.jms.*;
import javax.net.ssl.SSLContext;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class Sink implements MessageListener {
    public static void main(String[] args) throws Exception {

        String url = "amqps://bouvet:63002";
        String receiveQueue = "king_gustaf";
        String keystorePath = "./tmp/keys/king_gustaf.p12";
        String keystorePassword = "password";
        String keyPassword = "password";
        String trustStorePath = "./tmp/keys/truststore.jks";
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
			int delay = -1;
			if (message.getStringProperty("when") != null) {
				try {
					delay = (int) ZonedDateTime.parse(message.getStringProperty("when")).until(ZonedDateTime.now(), ChronoUnit.MILLIS);
				} catch (Exception e) {
					System.err.println("Could not parse \"when\"-field to calculate delay; " + message.getStringProperty("when"));
				}
			}
			System.out.println("Message received");
			try {
				System.out.println(((TextMessage)message).getText() + " delay " + delay + " ms");
			} catch (JMSException e) {
				throw new RuntimeException(e);
			}
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
    }
}
