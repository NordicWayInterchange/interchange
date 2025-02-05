package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "local_connections", uniqueConstraints = @UniqueConstraint(columnNames = {"source"}))
public class LocalConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "local_connection_seq")
    @Column(name = "id")
    private Integer id;

    private String source;

    public LocalConnection() {

    }

    public LocalConnection(Integer id, String source) {
        this.id = id;
        this.source = source;
    }

    public LocalConnection(String source) {
        this(null,source);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalConnection that = (LocalConnection) o;
        return Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }
}
