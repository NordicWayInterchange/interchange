package no.vegvesen.ixn;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import org.apache.qpid.jms.JmsConnectionFactory;

import javax.net.ssl.SSLContext;

//TODO we might set exceptionListener on the connectionfactory or the factory
//also, can we use the same factory for different hosts?
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

}
