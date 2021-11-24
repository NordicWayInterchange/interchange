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
    private Integer sub_id;

    @Enumerated(EnumType.STRING)
    private LocalSubscriptionStatus status = LocalSubscriptionStatus.REQUESTED;

    @JoinColumn(name = "sel_id", foreignKey = @ForeignKey(name = "fk_locsub_sel"))
    @Column(columnDefinition="TEXT")
    private String selector = "";

    @Column
    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    private String consumerCommonName;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "locend_id", foreignKey = @ForeignKey(name = "fk_locend_sub"))
    private Set<LocalEndpoint> localEndpoints = new HashSet<>();

    public LocalSubscription() {

    }

    public LocalSubscription(LocalSubscriptionStatus status, String selector, String consumerCommonName) {
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
    }

    public LocalSubscription(LocalSubscriptionStatus status, String selector) {
        this.status = status;
        this.selector = selector;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector) {
        this.sub_id = id;
        this.status = status;
        this.selector = selector;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector, String consumerCommonName) {
        this.sub_id = id;
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector, LocalDateTime lastUpdated) {
        this.sub_id = id;
        this.status = status;
        this.selector = selector;
        this.lastUpdated = lastUpdated;
    }

    public LocalSubscription(Integer sub_id, LocalSubscriptionStatus status, String selector, LocalDateTime lastUpdated, String consumerCommonName, Set<LocalEndpoint> localEndpoints) {
        this.sub_id = sub_id;
        this.status = status;
        this.selector = selector;
        this.lastUpdated = lastUpdated;
        this.consumerCommonName = consumerCommonName;
        this.localEndpoints = localEndpoints;
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

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
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
                Objects.equals(selector, that.selector) &&
                Objects.equals(consumerCommonName, that.consumerCommonName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, selector, consumerCommonName);
    }

    public Integer getSub_id() {
        return sub_id;
    }

    @Override
    public String toString() {
        return "LocalSubscription{" +
                "sub_id=" + sub_id +
                ", status=" + status +
                ", selector=" + selector +
                ", queueConsumerUser=" + consumerCommonName +
                '}';
    }

    public LocalSubscription withStatus(LocalSubscriptionStatus newStatus) {
        if (newStatus.equals(this.status)) {
            return this;
        } else {
            return new LocalSubscription(sub_id, newStatus, selector, consumerCommonName);
        }
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setSub_id(Integer sub_id) {
        this.sub_id = sub_id;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
