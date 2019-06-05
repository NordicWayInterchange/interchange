package no.vegvesen.interchange;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.qpid.jms.JmsConnectionFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.Hashtable;

public class Sink implements MessageListener {
    public static void main(String[] args) throws NamingException, JMSException {

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

        SSLContext context = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);


        Hashtable<Object, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        env.put("connectionfactory.myFactoryLookupTLS", url);
        env.put("queue.receiveQueue", receiveQueue);
        Context initialContext = new InitialContext(env);
        JmsConnectionFactory factory = (JmsConnectionFactory) initialContext.lookup("myFactoryLookupTLS");
        factory.setPopulateJMSXUserID(true);
        factory.setSslContext(context);
        Connection connection = factory.createConnection();
        Destination destination = (Destination) initialContext.lookup("receiveQueue");
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
