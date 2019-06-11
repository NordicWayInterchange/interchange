package no.vegvesen.interchange;

import no.vegvesen.ixn.IxnContext;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Source implements AutoCloseable {

    /**
     * A message source. Sends a single message, and exits.
     * TODO Take out the paths and stuff, move to args or properties so it can be run as a standalone (container?)
     * @param args
     */
    public static void main(String[] args) throws NamingException, JMSException {
        String url = "amqps://bouvet:5600";
        String sendQueue = "remote";
        String keystorePath = "/interchange/tmp/keys/bouvet.p12";
        String keystorePassword = "password";
        String keyPassword = "password";
        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12, keyPassword);
        String trustStorePath = "/interchange/tmp/keys/truststore.jks";
        String trustStorePassword = "password";
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
    private Connection connection;
    private Session session;
    private Destination queueS;

    public Source(String url, String sendQueue, SSLContext context) {
        this.url = url;
        this.sendQueue = sendQueue;
        this.sslContext = context;
    }

    public void start() throws NamingException, JMSException {
        IxnContext context = new IxnContext(url, sendQueue, null);
        connection = context.createConnection(sslContext);
        queueS = context.getSendQueue();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }


    public void send(String messageText) throws JMSException {
        MessageProducer producer = session.createProducer(queueS);

        JmsTextMessage message = (JmsTextMessage) session.createTextMessage(messageText);
        message.getFacade().setUserId("localhost");
        message.setStringProperty("who", "Norwegian Public Roads Administration");
        message.setStringProperty("how", "datex2");
        message.setStringProperty("what", "Obstruction");
        message.setStringProperty("version", "1.0");
        message.setStringProperty("lat", "60.352374");
        message.setStringProperty("lon", "13.334253");
        message.setStringProperty("where", "SE");
        message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        producer.send(message);

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
}
