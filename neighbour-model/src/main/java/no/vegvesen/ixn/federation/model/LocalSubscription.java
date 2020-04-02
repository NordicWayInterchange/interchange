package no.vegvesen.ixn.federation.model;

import javax.persistence.*;

//TODO Equals and Hashcode!!!
@Entity
@Table(name = "local_subscriptions")
public class LocalSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locsub_generator")
    @SequenceGenerator(name="locsub_generator", sequenceName = "locsub_seq")
    @Column(name="locsub_id")
    private Integer sub_id;

    @Enumerated(EnumType.STRING)
    private LocalSubscriptionStatus status = LocalSubscriptionStatus.REQUESTED;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "locsub_dat_id", referencedColumnName = "dat_id", foreignKey = @ForeignKey(name = "fk_locsub_dat"))
    private DataType dataType;

    public LocalSubscription() {

    }

    public LocalSubscription(LocalSubscriptionStatus status, DataType dataType) {
        this.status = status;
        this.dataType = dataType;
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

    public DataType getDataType() {
        return dataType;
    }

    //TODO lag et objekt av selector??
    public String bindKey() {
        return "" + dataType.toSelector().hashCode();
    }
}
