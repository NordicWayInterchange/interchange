package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "local_connections", uniqueConstraints = @UniqueConstraint(columnNames = {"source", "destination"}))
public class LocalConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "local_connection_seq")
    @Column(name = "id")
    private Integer id;

    private String source;

    private String destination;

    public LocalConnection() {

    }

    public LocalConnection(Integer id, String source, String destination) {
        this.id = id;
        this.source = source;
        this.destination = destination;
    }

    public LocalConnection(String source, String destination) {
        this(null,source,destination);
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

    public Integer getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalConnection that = (LocalConnection) o;
        return Objects.equals(source, that.source) && Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }
}
