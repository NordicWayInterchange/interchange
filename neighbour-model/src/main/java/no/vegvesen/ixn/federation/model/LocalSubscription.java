package no.vegvesen.ixn.federation.model;

import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

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

    private boolean createNewQueue;

    private String queueConsumerUser;

    public LocalSubscription() {

    }

    public LocalSubscription(LocalSubscriptionStatus status, String selector) {
        this.status = status;
        this.selector = selector;
    }

    public LocalSubscription(LocalSubscriptionStatus status, String selector, boolean createNewQueue, String queueConsumerUser) {
        this.status = status;
        this.selector = selector;
        this.createNewQueue = createNewQueue;
        this.queueConsumerUser = queueConsumerUser;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector) {
        this.sub_id = id;
        this.status = status;
        this.selector = selector;
    }

    public LocalSubscription(Integer id, LocalSubscriptionStatus status, String selector, LocalDateTime lastUpdated) {
        this.sub_id = id;
        this.status = status;
        this.selector = selector;
        this.lastUpdated = lastUpdated;
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

    public boolean isCreateNewQueue() {
        return createNewQueue;
    }

    public void setCreateNewQueue(boolean createNewQueue) {
        this.createNewQueue = createNewQueue;
    }

    public String getQueueConsumerUser() {
        return queueConsumerUser;
    }

    public void setQueueConsumerUser(String queueConsumerUser) {
        this.queueConsumerUser = queueConsumerUser;
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

    public Integer getSub_id() {
        return sub_id;
    }

    @Override
    public String toString() {
        return "LocalSubscription{" +
                "sub_id=" + sub_id +
                ", status=" + status +
                ", selector=" + selector +
                ", createNewQueue=" + createNewQueue +
                ", queueConsumerUser=" + queueConsumerUser +
                '}';
    }

    public LocalSubscription withStatus(LocalSubscriptionStatus newStatus) {
        if (newStatus.equals(this.status)) {
            return this;
        } else {
            return new LocalSubscription(sub_id, newStatus, selector);
        }
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}
