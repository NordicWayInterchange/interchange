package no.vegvesen.ixn.federation.model;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "local_subscriptions")
public class LocalSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locsub_seq")
    @Column(name="id")
    private Integer id;

    @Column(nullable = false)
    private String uuid = UUID.randomUUID().toString();

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

    @Column
    private String description;

    // ErrorMessage is needed for sending the error message back to the user
    // Any subscription with an error message is deleted shortly after creation
    @Column
    private String errorMessage;

    public LocalSubscription() {
    }

    public LocalSubscription(String selector, String consumerCommonName, String description){
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.description = description;
    }

    public LocalSubscription(LocalSubscriptionStatus status, String selector, String consumerCommonName) {
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector, String consumerCommonName) {
        this.id = id;
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector, String consumerCommonName, Set<LocalEndpoint> localEndpoints) {
        this.id = id;
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.localEndpoints.addAll(localEndpoints);
    }
    public LocalSubscription(LocalSubscriptionStatus status, String selector, String consumerCommonName, Set<LocalEndpoint> localEndpoints) {
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.localEndpoints.addAll(localEndpoints);
    }

    public LocalSubscription(String uuid, LocalSubscriptionStatus status, String selector, String consumerCommonName, Set<LocalEndpoint> localEndpoints) {
        this.uuid = uuid;
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.localEndpoints.addAll(localEndpoints);
    }

    public LocalSubscription(String uuid, LocalSubscriptionStatus status, String selector, String consumerCommonName, Set<LocalEndpoint> localEndpoints, String description) {
        this.uuid = uuid;
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.description = description;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public void addLocalEndpoint(LocalEndpoint newEndpoint) {
        localEndpoints.add(newEndpoint);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer sub_id) {
        this.id = sub_id;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalSubscription that = (LocalSubscription) o;
        return Objects.equals(selector, that.selector) &&
                Objects.equals(consumerCommonName, that.consumerCommonName) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector, consumerCommonName, description);
    }

    @Override
    public String toString() {
        return "LocalSubscription{" +
                "id=" + id +
                "uuid=" + uuid +
                ", status=" + status +
                ", selector=" + selector +
                ", consumerCommonName=" + consumerCommonName +
                ", errorMessage=" + errorMessage +
                ", description=" + description +
                '}';
    }
}
