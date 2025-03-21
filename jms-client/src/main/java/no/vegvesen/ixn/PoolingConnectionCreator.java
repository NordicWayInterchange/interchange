package no.vegvesen.ixn;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;

import java.util.concurrent.ConcurrentHashMap;

public class PoolingConnectionCreator implements ConnectionCreator {

    private final ConnectionCreator connectionCreatorDelegate;
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public PoolingConnectionCreator(ConnectionCreator connectionCreatorDelegate) {
        this.connectionCreatorDelegate = connectionCreatorDelegate;
    }

    public Connection createConnection(String url) {
        return connections.computeIfAbsent(url, connectionCreatorDelegate::createConnection);
    }

    public void close() {
        connections.forEach((s, c) -> {
            try {
                c.close();
            } catch (JMSException e) {
                System.out.println("Exception while closing connection: " + e);
            }
        });
    }

}
