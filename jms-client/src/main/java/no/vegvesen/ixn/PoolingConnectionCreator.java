package no.vegvesen.ixn;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class PoolingConnectionCreator implements ConnectionCreator {

    private static final Logger logger = LoggerFactory.getLogger(PoolingConnectionCreator.class);
    private final ConnectionCreator connectionCreatorDelegate;
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public PoolingConnectionCreator(ConnectionCreator connectionCreatorDelegate) {
        this.connectionCreatorDelegate = connectionCreatorDelegate;
    }

    public Connection createConnection(String url) {
        return connections.computeIfAbsent(url, connectionCreatorDelegate::createConnection);
    }

    public void close() {
        connections.forEach((url, connection) -> {
            try {
                connection.close();
            } catch (JMSException e) {
                logger.debug("Exception while closing connection to url {}",url,e);
            }
        });
    }

}
