package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "local_delivery_connections", uniqueConstraints = @UniqueConstraint(columnNames = {"source", "destination"}))
public class LocalDeliveryConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loc_del_connection_seq")
    @Column(name = "id")
    private Integer id;

    private String source;

    private String destination;

    public LocalDeliveryConnection() {

    }

    public LocalDeliveryConnection(Integer id, String source, String destination) {
        this.id = id;
        this.source = source;
        this.destination = destination;
    }

    public LocalDeliveryConnection(String source, String destination) {
        this(null, source, destination);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalDeliveryConnection)) return false;
        LocalDeliveryConnection that = (LocalDeliveryConnection) o;
        return source.equals(that.source) && destination.equals(that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }
}
