package no.vegvesen.ixn;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;

import java.util.concurrent.ConcurrentHashMap;

public class SinkConnectionPool {

    private final ConnectionCreator connectionCreator;
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public SinkConnectionPool(ConnectionCreator connectionCreator) {
        this.connectionCreator = connectionCreator;
    }

    public Connection createConnection(String url) {
        return connections.computeIfAbsent(url, connectionCreator::createConnection);
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
