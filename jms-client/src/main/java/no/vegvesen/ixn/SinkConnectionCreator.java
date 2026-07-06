package no.vegvesen.ixn;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy;

import javax.net.ssl.SSLContext;

public class SinkConnectionCreator {

    private final JmsConnectionFactory jmsConnectionFactory;

    public SinkConnectionCreator(String uri, SSLContext sslContext, Integer prefetch) {
        jmsConnectionFactory = new JmsConnectionFactory(uri);
        jmsConnectionFactory.setSslContext(sslContext);
        if (prefetch != null) {
            JmsDefaultPrefetchPolicy prefetchPolicy = new JmsDefaultPrefetchPolicy();
            prefetchPolicy.setAll(prefetch);
            jmsConnectionFactory.setPrefetchPolicy(prefetchPolicy);
        }
        jmsConnectionFactory.setPopulateJMSXUserID(true);
    }

    public Connection createConnection() throws JMSException {
        return jmsConnectionFactory.createConnection();
    }
}
