package no.vegvesen.interchange;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;

public class Source {

    /**
     * A message source. Sends a single message, and exits.
     * TODO Take out the paths and stuff, move to args or properties so it can be run as a standalone (container?)
     * @param args
     */
    public static void main(String[] args) throws NamingException, JMSException {
        KeystoreDetails keystoreDetails = new KeystoreDetails("C:\\temp_checkout\\interchange\\tmp\\keys\\bouvet.p12",
                "password",
                KeystoreType.PKCS12,"password");
        KeystoreDetails trustStoreDetails = new KeystoreDetails("c:\\temp_checkout\\interchange\\tmp\\keys\\truststore.jks",
                "password",KeystoreType.JKS);

        SSLContext context = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);

        String url = String.format("amqps://%s:%s","remote","5601");

        Hashtable<Object, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        env.put("connectionfactory.myFactoryLookupTLS", url);
        env.put("queue.sendQueue", "fedEx");

        Context initialContext = new InitialContext(env);
        JmsConnectionFactory factory = (JmsConnectionFactory) initialContext.lookup("myFactoryLookupTLS");
        factory.setPopulateJMSXUserID(true);
        factory.setSslContext(context);
        Connection connection = factory.createConnection();
        Destination queueS = (Destination) initialContext.lookup("sendQueue");
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(queueS);

        JmsTextMessage message = (JmsTextMessage) session.createTextMessage("Yo");
        message.getFacade().setUserId("localhost");
        message.setStringProperty("who", "Norwegian Public Roads Administration");
        message.setStringProperty("how", "datex2");
		message.setStringProperty("what", "Obstruction");
		message.setStringProperty("version", "1.0");
		message.setStringProperty("lat", "63.0");
		message.setStringProperty("lon", "10.0");
		message.setStringProperty("where1", "NO");
		message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

		producer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE, new CompletionListener() {
            @Override
            public void onCompletion(Message message) {
                System.out.println("Message completion done for message");
            }

            @Override
            public void onException(Message message, Exception e) {
                System.out.println("Exception returned for message");
                e.printStackTrace();

            }
        });
		//connection.close();
    }

}
