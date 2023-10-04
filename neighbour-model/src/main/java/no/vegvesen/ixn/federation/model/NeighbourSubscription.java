package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "neighbour_subscriptions")
public class NeighbourSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "neigh_sub_seq")
    private Integer id;

    @Enumerated(EnumType.STRING)
    private NeighbourSubscriptionStatus subscriptionStatus;

    @Column(columnDefinition="TEXT")
    private String selector;

    private String path;

    @Column(columnDefinition="TEXT")
    private String consumerCommonName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "end_id", foreignKey = @ForeignKey(name = "fk_end_neigh_sub"))
    private Set<NeighbourEndpoint> endpoints = new HashSet<>();

    private long lastUpdatedTimestamp;

    private String queueName;

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

    public NeighbourSubscription(String selector, NeighbourSubscriptionStatus neighbourSubscriptionStatus, String consumerCommonName, String queueName) {
        this.selector = selector;
        this.subscriptionStatus = neighbourSubscriptionStatus;
        this.consumerCommonName = consumerCommonName;
        this.queueName = queueName;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
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
                "id=" + id +
                ", subscriptionStatus=" + subscriptionStatus +
                ", selector='" + selector + '\'' +
                ", path='" + path + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", endpoints=" + endpoints +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", queueName='" + queueName + '\'' +
                '}';
    }

    public String bindKey() {
        return "" + selector.hashCode();
    }
}
