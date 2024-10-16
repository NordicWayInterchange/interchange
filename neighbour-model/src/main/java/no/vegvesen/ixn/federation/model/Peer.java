package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "peers")
public class Peer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "peer_seq")
    private Integer id;

    private String uuid = UUID.randomUUID().toString();

    private String name;

    public Peer() {

    }

    public Peer(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return Objects.equals(uuid, peer.uuid) && Objects.equals(name, peer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name);
    }

    @Override
    public String toString() {
        return "Peer{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
