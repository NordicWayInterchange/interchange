package no.vegvesen.ixn;

import jakarta.jms.Connection;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import org.apache.qpid.jms.JmsConnectionFactory;

import javax.net.ssl.SSLContext;

//TODO we need to have map of URL -> connection, and return the already created connection if it's already created
public class NewSink {


    private final SSLContext context;

    public NewSink(SSLContext context) {
        this.context = context;
    }


    public Connection createConnection(String url) throws JMSException {
        JmsConnectionFactory connectionFactory = new JmsConnectionFactory(url);
        connectionFactory.setSslContext(context);
        return connectionFactory.createConnection();
    }

    public Connection createConnection(String url, ExceptionListener exceptionListener) throws JMSException {
        Connection connection = createConnection(url);
        connection.setExceptionListener(exceptionListener);
        return connection;
    }
}
