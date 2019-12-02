package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.Properties;

import static no.vegvesen.ixn.Source.getProperties;

public class Sink implements MessageListener, AutoCloseable {


    public static void main(String[] args) throws Exception {
		Properties props = getProperties(args, "/sink.properties");

		String url = props.getProperty("sink.url");
        String receiveQueue = props.getProperty("sink.receiveQueue");
        String keystorePath = props.getProperty("sink.keyStorepath");
        String keystorePassword = props.getProperty("sink.keyStorepass");
        String keyPassword = props.getProperty("sink.keypass");
        String trustStorePath = props.getProperty("sink.trustStorepath");
        String truststorePassword = props.getProperty("sink.trustStorepass");

        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12, keyPassword);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                truststorePassword,KeystoreType.JKS);

        SSLContext sslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);

        Sink sink = new Sink(url,receiveQueue,sslContext);
		System.out.println(String.format("Listening for messages from queue [%s] on server [%s]", receiveQueue, url));
        sink.start();
    }

    protected final String url;
    private final String queueName;
    private final SSLContext sslContext;
    protected Connection connection;

    public Sink(String url, String queueName, SSLContext sslContext) {
        this.url = url;
        this.queueName = queueName;
        this.sslContext = sslContext;
    }


    public void start() throws JMSException, NamingException {
		MessageConsumer consumer = createConsumer();
		consumer.setMessageListener(this);
    }

	public MessageConsumer createConsumer() throws NamingException, JMSException {
		IxnContext ixnContext = new IxnContext(this.url,null, this.queueName);
		createConnection(ixnContext);
		Destination destination = ixnContext.getReceiveQueue();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		return session.createConsumer(destination);
	}

	protected void createConnection(IxnContext ixnContext) throws NamingException, JMSException {
		connection = ixnContext.createConnection(sslContext);
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
			System.out.println("** Message received **");
			Enumeration messageNames =  message.getPropertyNames();

			while (messageNames.hasMoreElements()) {
				String messageName = (String) messageNames.nextElement();
				String value = message.getStringProperty(messageName);
				System.out.println(String.format("%s:%s",messageName,value));
			}

			try {
				System.out.println(((TextMessage)message).getText() + " delay " + delay + " ms \n");
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
