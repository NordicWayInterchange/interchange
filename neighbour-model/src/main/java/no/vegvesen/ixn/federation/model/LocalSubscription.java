package no.vegvesen.ixn.federation.model;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
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
    @Column(columnDefinition="TEXT", nullable = false)
    private String selector;

    @Column
    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    @Column(columnDefinition="TEXT", nullable = false)
    private String consumerCommonName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "locend_id", foreignKey = @ForeignKey(name = "fk_locend_sub"))
    private Set<LocalEndpoint> localEndpoints = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "loccon_id", foreignKey = @ForeignKey(name = "fk_loccon_sub"))
    private Set<LocalConnection> connections = new HashSet<>();

    // ErrorMessage is needed for sending the error message back to the user
    // Any subscription with an error message is deleted shortly after creation
    @Column
    private String errorMessage;

    public LocalSubscription() {
    }

    public LocalSubscription(String selector, String consumerCommonName) {
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
    }

    public LocalSubscription(LocalSubscriptionStatus status, String selector, String consumerCommonName) {
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
    }

    public LocalSubscription(Integer id, String selector, String consumerCommonName) {
        this.id = id;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector, String consumerCommonName) {
        this.id = id;
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
    }


    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector, String consumerCommonName, Set<LocalConnection> connections, Set<LocalEndpoint> localEndpoints) {
        this.id = id;
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.connections.addAll(connections);
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

    public Set<LocalEndpoint> getLocalEndpoints() {
        return localEndpoints;
    }

    public void setLocalEndpoints(Set<LocalEndpoint> newLocalEndpoints) {
        this.localEndpoints.clear();
        if (newLocalEndpoints != null) {
            this.localEndpoints.addAll(newLocalEndpoints);
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    //TODO lag et objekt av selector??
    public String bindKey() {
        return "" + selector.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalSubscription that = (LocalSubscription) o;
        return Objects.equals(selector, that.selector) &&
                Objects.equals(consumerCommonName, that.consumerCommonName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector, consumerCommonName);
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
                ", consumerCommonName=" + consumerCommonName +
                ", errorMessage=" + errorMessage +
                '}';
    }

    public LocalSubscription withStatus(LocalSubscriptionStatus newStatus) {
        if (newStatus.equals(this.status)) {
            return this;
        } else {
            this.status = newStatus;
            return this;
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
