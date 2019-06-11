package no.vegvesen.interchange;

import no.vegvesen.ixn.IxnContext;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class Sink implements MessageListener, AutoCloseable {


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

        Sink sink = new Sink(url,receiveQueue,sslContext);
        sink.start();
    }

    private final String url;
    private final String queueName;
    private final SSLContext sslContext;
    private Connection connection;

    public Sink(String url, String queueName, SSLContext sslContext) {
        this.url = url;
        this.queueName = queueName;
        this.sslContext = sslContext;
    }


    public void start() throws JMSException, NamingException {
        IxnContext ixnContext = new IxnContext(url,null,queueName);
        connection = ixnContext.createConnection(sslContext);
        Destination destination = ixnContext.getReceiveQueue();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(this);
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

    @Override
    public void close() throws Exception {
        if (connection != null)  {
            connection.close();
        }

    }
}
