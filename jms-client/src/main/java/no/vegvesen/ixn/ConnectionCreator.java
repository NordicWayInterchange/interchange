package no.vegvesen.ixn;

import jakarta.jms.Connection;

public interface ConnectionCreator {
    Connection createConnection(String url);
}
