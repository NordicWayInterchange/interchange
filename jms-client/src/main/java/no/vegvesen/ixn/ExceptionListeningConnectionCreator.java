package no.vegvesen.ixn;

import jakarta.jms.Connection;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import no.vegvesen.ixn.SinkConnectionPool.ConnectionCreator;

public class ExceptionListeningConnectionCreator implements ConnectionCreator {
    private final NewSink sink;
    private final ExceptionListener exceptionListener;

    public ExceptionListeningConnectionCreator(NewSink sink, ExceptionListener exceptionListener) {
        this.sink = sink;
        this.exceptionListener = exceptionListener;
    }

    @Override
    public Connection createConnection(String url) {
        try {
            Connection conn = sink.createConnection(url, exceptionListener);
            conn.start();
            return conn;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
