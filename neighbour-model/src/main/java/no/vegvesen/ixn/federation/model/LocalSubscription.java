package no.vegvesen.ixn.federation.model;

import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "local_subscriptions")
public class LocalSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locsub_seq")
    @Column(name="id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    private LocalSubscriptionStatus status = LocalSubscriptionStatus.REQUESTED;

    @JoinColumn(name = "sel_id", foreignKey = @ForeignKey(name = "fk_locsub_sel"))
    @Column(columnDefinition="TEXT")
    private String selector = "";

    @Column
    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    @Column(columnDefinition="TEXT")
    private String consumerCommonName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "locend_id", foreignKey = @ForeignKey(name = "fk_locend_sub"))
    private Set<LocalEndpoint> localEndpoints = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "loccon_id", foreignKey = @ForeignKey(name = "fk_loccon_sub"))
    private Set<LocalConnection> connections = new HashSet<>();

    public LocalSubscription() {

    }


    public LocalSubscription(LocalSubscriptionStatus status, String selector) {
        this.status = status;
        this.selector = selector;
    }

    public LocalSubscription(LocalSubscriptionStatus status, String selector, String consumerCommonName) {
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector) {
        this.id = id;
        this.status = status;
        this.selector = selector;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector, LocalDateTime lastUpdated) {
        this.id = id;
        this.status = status;
        this.selector = selector;
        this.lastUpdated = lastUpdated;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector, LocalDateTime lastUpdated, Set<LocalEndpoint> localEndpoints) {
        this.id = id;
        this.status = status;
        this.selector = selector;
        this.lastUpdated = lastUpdated;
        this.localEndpoints.addAll(localEndpoints);
    }

    public void setStatus(LocalSubscriptionStatus status) {
        this.status = status;
    }

    public LocalSubscriptionStatus getStatus() {
        return status;
    }

    public boolean isSubscriptionWanted() {
        return status.equals(LocalSubscriptionStatus.REQUESTED)
                || status.equals(LocalSubscriptionStatus.CREATED);
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public Set<LocalEndpoint> getLocalEndpoints() {
        return localEndpoints;
    }

    public void setLocalEndpoints(Set<LocalEndpoint> newLocalEndpoints) {
        this.localEndpoints.clear();
        if (newLocalEndpoints != null) {
            this.localEndpoints.addAll(newLocalEndpoints);
        }
    }

    public Set<LocalConnection> getConnections() {
        return connections;
    }

    public void setConnections(Set<LocalConnection> connections) {
        this.connections = connections;
    }

    public void addConnection(LocalConnection connection) {
        connections.add(connection);
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    //TODO lag et objekt av selector??
    public String bindKey() {
        return "" + selector.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalSubscription that = (LocalSubscription) o;
        return status == that.status &&
                Objects.equals(selector, that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, selector);
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "LocalSubscription{" +
                "id=" + id +
                ", status=" + status +
                ", selector=" + selector +
                '}';
    }

    public LocalSubscription withStatus(LocalSubscriptionStatus newStatus) {
        if (newStatus.equals(this.status)) {
            return this;
        } else {
            return new LocalSubscription(id,newStatus,selector,lastUpdated,localEndpoints);
        }
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setId(Integer sub_id) {
        this.id = sub_id;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
