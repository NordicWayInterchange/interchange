package no.vegvesen.ixn.federation.model;

import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "local_deliveries")
public class LocalDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locdel_seq")
    @Column(name="id")
    private String id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "locdelend_id", foreignKey = @ForeignKey(name = "fk_locdel_end"))
    private Set<LocalDeliveryEndpoint> endpoints;

    @Column
    private String path;

    @JoinColumn(name = "sel_id", foreignKey = @ForeignKey(name = "fk_locdel_sel"))
    @Column(columnDefinition="TEXT")
    private String selector;

    @Column
    @UpdateTimestamp
    private LocalDateTime lastUpdatedTimestamp;

    @Enumerated(EnumType.STRING)
    private LocalDeliveryStatus status = LocalDeliveryStatus.REQUESTED;

    public LocalDelivery() {
    }

    public LocalDelivery(String id, Set<LocalDeliveryEndpoint> endpoints, String path, String selector, LocalDateTime lastUpdatedTimestamp, LocalDeliveryStatus status) {
        this.id = id;
        this.endpoints = endpoints;
        this.path = path;
        this.selector = selector;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<LocalDeliveryEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<LocalDeliveryEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public LocalDateTime getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(LocalDateTime lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public LocalDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(LocalDeliveryStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalDelivery delivery = (LocalDelivery) o;
        return lastUpdatedTimestamp == delivery.lastUpdatedTimestamp && Objects.equals(id, delivery.id) && Objects.equals(endpoints, delivery.endpoints) && Objects.equals(path, delivery.path) && Objects.equals(selector, delivery.selector) && status == delivery.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, endpoints, path, selector, lastUpdatedTimestamp, status);
    }

    @Override
    public String toString() {
        return "LocalDelivery{" +
                "id='" + id + '\'' +
                ", endpoints=" + endpoints +
                ", path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", status=" + status +
                '}';
    }
}
