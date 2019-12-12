package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
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
     * @param args
     */
    public static void main(String[] args) throws NamingException, JMSException, IOException {
		Properties props = getProperties(args, "/source.properties");

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

	static Properties getProperties(String[] args, String propertiesFile) throws IOException {
		InputStream in;
		if (args.length == 1) {
			in = new FileInputStream(args[0]);
		} else {
			in = Source.class.getResourceAsStream(propertiesFile);
		}
		Properties props = new Properties();
		props.load(in);
		return props;
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

        JmsTextMessage message = createTextMessage(messageText);
        message.getFacade().setUserId("localhost");
        message.setStringProperty("who", "Norwegian Public Roads Administration");
        message.setStringProperty("messageType", "datex2");
        message.setStringProperty("what", "Obstruction");
        message.setStringProperty("version", "1.0");
        message.setStringProperty("lat", "60.352374");
        message.setStringProperty("lon", "13.334253");
        message.setStringProperty("originatingCountry", "SE");
        message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        sendTextMessage(message, Message.DEFAULT_TIME_TO_LIVE);
    }

	public void sendTextMessage(JmsTextMessage message, long timeToLive) throws JMSException {
		MessageProducer producer = session.createProducer(queueS);
		producer.send(message,  DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, timeToLive);
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

	public JmsTextMessage createTextMessage(String msg) throws JMSException {
		return (JmsTextMessage) session.createTextMessage(msg);
	}

}
