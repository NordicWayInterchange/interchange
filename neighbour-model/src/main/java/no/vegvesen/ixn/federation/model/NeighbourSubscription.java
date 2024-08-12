package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "neighbour_subscriptions")
public class NeighbourSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "neigh_sub_seq")
    private Integer id;

    private String uuid = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    private NeighbourSubscriptionStatus subscriptionStatus;

    @Column(columnDefinition="TEXT")
    private String selector;

    private String path;

    @Column(columnDefinition="TEXT")
    private String consumerCommonName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "neigh_end_id", foreignKey = @ForeignKey(name = "fk_end_neigh_sub"))
    private Set<NeighbourEndpoint> endpoints = new HashSet<>();

    private long lastUpdatedTimestamp;

    public NeighbourSubscription() {

    }

    public NeighbourSubscription(String selector, NeighbourSubscriptionStatus subscriptionStatus) {
        this.selector = selector;
        this.subscriptionStatus = subscriptionStatus;
    }

    public NeighbourSubscription(String selector, NeighbourSubscriptionStatus subscriptionStatus, String consumerCommonName) {
        this.selector = selector;
        this.subscriptionStatus = subscriptionStatus;
        this.consumerCommonName = consumerCommonName;
    }

    public NeighbourSubscription(NeighbourSubscriptionStatus subscriptionStatus, String selector, String path, String consumerCommonName, Set<NeighbourEndpoint> endpoints) {
        this.subscriptionStatus = subscriptionStatus;
        this.selector = selector;
        this.path = path;
        this.consumerCommonName = consumerCommonName;
        this.endpoints.addAll(endpoints);
    }

    public NeighbourSubscription(int id, NeighbourSubscriptionStatus subscriptionStatus, String selector, String path, String consumerCommonName) {
        this.id = id;
        this.subscriptionStatus = subscriptionStatus;
        this.selector = selector;
        this.path = path;
        this.consumerCommonName = consumerCommonName;
    }

    public NeighbourSubscription(String uuid, NeighbourSubscriptionStatus subscriptionStatus, String selector, String path, String consumerCommonName, Set<NeighbourEndpoint> endpoints) {
        this.subscriptionStatus = subscriptionStatus;
        this.selector = selector;
        this.path = path;
        this.consumerCommonName = consumerCommonName;
        this.endpoints.addAll(endpoints);
        this.uuid = uuid;
    }

    public NeighbourSubscription(Integer id, NeighbourSubscriptionStatus subscriptionStatus, String selector, String path, String consumerCommonName, Set<NeighbourEndpoint> endpoints) {
        this.id = id;
        this.subscriptionStatus = subscriptionStatus;
        this.selector = selector;
        this.path = path;
        this.consumerCommonName = consumerCommonName;
        this.endpoints.addAll(endpoints);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public NeighbourSubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(NeighbourSubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    public Set<NeighbourEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<NeighbourEndpoint> newEndpoints) {
        this.endpoints.clear();
        if (newEndpoints != null) {
            this.endpoints.addAll(newEndpoints);
        }
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public boolean isSharded() {
        return selector.contains("shardId");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NeighbourSubscription)) return false;
        NeighbourSubscription that = (NeighbourSubscription) o;
        return selector.equals(that.selector) &&
                consumerCommonName.equals(that.consumerCommonName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector, consumerCommonName);
    }

    @Override
    public String toString() {
        return "NeighbourSubscription{" +
                "id=" + uuid +
                ", subscriptionStatus=" + subscriptionStatus +
                ", selector='" + selector + '\'' +
                ", path='" + path + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", endpoints=" + endpoints +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp + '\'' +
                '}';
    }
}
