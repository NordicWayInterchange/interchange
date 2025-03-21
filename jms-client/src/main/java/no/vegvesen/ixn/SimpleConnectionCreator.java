package no.vegvesen.ixn;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import org.apache.qpid.jms.JmsConnectionFactory;

import javax.net.ssl.SSLContext;

public class SimpleConnectionCreator implements ConnectionCreator {

    private final SSLContext context;

    public SimpleConnectionCreator(SSLContext context) {
        this.context = context;
   }

    @Override
    public Connection createConnection(String url) {
        try {
            JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(url);
            jmsConnectionFactory.setSslContext(context);
            Connection connection = jmsConnectionFactory.createConnection();
            connection.start();
            return connection;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
