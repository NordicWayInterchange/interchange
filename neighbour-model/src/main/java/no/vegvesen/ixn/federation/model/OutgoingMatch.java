package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "outgoing_matches")
public class OutgoingMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "out_matches_seq")
    private Integer id;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "loc_del", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_out_match_local_delivery"))
    private LocalDelivery localDelivery;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "cap", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_out_match_capability"))
    private Capability capability;

    @Enumerated(EnumType.STRING)
    private OutgoingMatchStatus status;

    private String serviceProviderName;

    private String deliveryQueueName;

    @Column(length = 1024)
    private String selector;

    public OutgoingMatch() {

    }

    public OutgoingMatch(LocalDelivery localDelivery, Capability capability, OutgoingMatchStatus status) {
        this.localDelivery = localDelivery;
        this.capability = capability;
        this.status = status;
    }

    public OutgoingMatch(LocalDelivery localDelivery, Capability capability, String serviceProviderName, OutgoingMatchStatus status) {
        this.localDelivery = localDelivery;
        this.capability = capability;
        this.serviceProviderName = serviceProviderName;
        this.status = status;
    }

    public OutgoingMatch(LocalDelivery localDelivery, Capability capability, String serviceProviderName, String deliveryQueueName, OutgoingMatchStatus status) {
        this.localDelivery = localDelivery;
        this.capability = capability;
        this.serviceProviderName = serviceProviderName;
        this.deliveryQueueName = deliveryQueueName;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public LocalDelivery getLocalDelivery() {
        return localDelivery;
    }

    public void setLocalDelivery(LocalDelivery localDelivery) {
        this.localDelivery = localDelivery;
    }

    public Capability getCapability() {
        return capability;
    }

    public void setCapability(Capability capability) {
        this.capability = capability;
    }

    public OutgoingMatchStatus getStatus() {
        return status;
    }

    public void setStatus(OutgoingMatchStatus status) {
        this.status = status;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public String getDeliveryQueueName() {
        return deliveryQueueName;
    }

    public void setDeliveryQueueName(String deliveryQueueName) {
        this.deliveryQueueName = deliveryQueueName;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String bindKey() {
        return "" + selector.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutgoingMatch that = (OutgoingMatch) o;
        return localDelivery.equals(that.localDelivery) && capability.equals(that.capability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localDelivery, capability);
    }

    @Override
    public String toString() {
        return "OutgoingMatch{" +
                "localDelivery=" + localDelivery +
                ", capability=" + capability +
                '}';
    }
}
