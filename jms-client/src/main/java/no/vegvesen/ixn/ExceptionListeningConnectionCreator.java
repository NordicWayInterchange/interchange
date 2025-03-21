package no.vegvesen.ixn;

import jakarta.jms.Connection;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import no.vegvesen.ixn.SinkConnectionPool.ConnectionCreator;
import org.apache.qpid.jms.JmsConnectionFactory;

import javax.net.ssl.SSLContext;

public class ExceptionListeningConnectionCreator implements ConnectionCreator {
    private final ExceptionListener exceptionListener;
    private final SSLContext context;

    public ExceptionListeningConnectionCreator(SSLContext context, ExceptionListener exceptionListener) {
        this.context = context;
        this.exceptionListener = exceptionListener;
    }

    @Override
    public Connection createConnection(String url) {
        try {
            JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(url);
            jmsConnectionFactory.setSslContext(context);
            jmsConnectionFactory.setExceptionListener(exceptionListener);
            Connection connection = jmsConnectionFactory.createConnection();
            connection.start();
            return connection;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
