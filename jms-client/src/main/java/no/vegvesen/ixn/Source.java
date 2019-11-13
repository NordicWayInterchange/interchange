package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class Source implements AutoCloseable {

	/**
     * A message source. Sends a single message, and exits.
     * Note that the main file does not make use of any Spring Boot stuff.
     * However, an instance of the class could easilly be used from Spring Boot, as all
     * dependent settings are handled in the main method, and passed as parameters to the instance.
     * @param args no args processed
     */
    public static void main(String[] args) throws NamingException, JMSException {

        String propertiesFile = "/source.properties";
        Properties props = new Properties();
        try (InputStream in = Source.class.getResourceAsStream(propertiesFile)) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException(String.format("The file %s could not be read",propertiesFile));
        }


        String url = props.getProperty("source.url");
        String sendQueue = props.getProperty("source.sendQueue");
        String keystorePath = props.getProperty("source.keyStorepath") ;
        String keystorePassword = props.getProperty("source.keyStorepass");
        String keyPassword = props.getProperty("source.keypass");
        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12, keyPassword);
        String trustStorePath = props.getProperty("source.trustStorepath");
        String trustStorePassword = props.getProperty("source.trustStorepass");
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                trustStorePassword,KeystoreType.JKS);

        SSLContext sslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);

        try( Source s = new Source(url,sendQueue,sslContext)) {
            s.start();
            s.send("Yo!");

        }
    }

	private final String url;
    private final String sendQueue;
    private final SSLContext sslContext;
    protected Connection connection;
    private Session session;
    private Destination queueS;

    public Source(String url, String sendQueue, SSLContext context) {
        this.url = url;
        this.sendQueue = sendQueue;
        this.sslContext = context;
    }

    public void start() throws NamingException, JMSException {
        IxnContext context = new IxnContext(url, sendQueue, null);
        createConnection(context);
        queueS = context.getSendQueue();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

	protected void createConnection(IxnContext ixnContext) throws NamingException, JMSException {
		connection = ixnContext.createConnection(sslContext);
	}

    public void send(String messageText) throws JMSException {
        TextMessage message = createTextMessage(messageText);
		message.setStringProperty("who", "Norwegian Public Roads Administration");
		message.setStringProperty("how", "datex2");
		message.setStringProperty("what", "Obstruction");
		message.setStringProperty("version", "1.0");
		message.setStringProperty("lat", "60.352374");
		message.setStringProperty("lon", "13.334253");
		message.setStringProperty("where", "SE");
		message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		sendTextMessage(message, Message.DEFAULT_TIME_TO_LIVE);
	}

	public void sendTextMessage(TextMessage message, long timeToLive) throws JMSException {
		MessageProducer producer = session.createProducer(queueS);
		producer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, timeToLive);
	}

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }

	public TextMessage createTextMessage(String msg) throws JMSException {
		return session.createTextMessage(msg);
	}

}
