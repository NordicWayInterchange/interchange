package no.vegvesen.ixn.federation.model;

import javax.persistence.*;

@Entity
@Table(name = "local_connections")
public class LocalConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "local_connection_seq")
    @Column(name = "id")
    private Integer id;

    private String source;

    private String destination;

    public LocalConnection() {

    }

    public LocalConnection(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
