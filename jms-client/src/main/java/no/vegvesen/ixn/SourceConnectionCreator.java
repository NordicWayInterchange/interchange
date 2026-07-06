package no.vegvesen.ixn;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import org.apache.qpid.jms.JmsConnectionFactory;

import javax.net.ssl.SSLContext;

public class SourceConnectionCreator {

    private final JmsConnectionFactory connectionFactory;

    public SourceConnectionCreator(String uri, SSLContext sslContext) {
        connectionFactory = new JmsConnectionFactory(uri);
        connectionFactory.setSslContext(sslContext);
        connectionFactory.setPopulateJMSXUserID(true);
    }

    public Connection createConnection() throws JMSException {
        return connectionFactory.createConnection();
    }
}
