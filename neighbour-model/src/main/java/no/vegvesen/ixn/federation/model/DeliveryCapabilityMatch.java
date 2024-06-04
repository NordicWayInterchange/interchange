package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "del_cap_matches")
public class DeliveryCapabilityMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "out_matches_seq")
    private Integer id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "loc_del", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_out_match_local_delivery"))
    private LocalDelivery localDelivery;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "cap", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_out_match_capability"))
    private Capability capability;

    private String serviceProviderName;

    public DeliveryCapabilityMatch() {

    }

    public DeliveryCapabilityMatch(LocalDelivery localDelivery, Capability capability, String serviceProviderName) {
        this.localDelivery = localDelivery;
        this.capability = capability;
        this.serviceProviderName = serviceProviderName;
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

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public boolean capabilityIsTearDown() {
        return capability.getStatus().equals(CapabilityStatus.TEAR_DOWN);
    }

    public boolean deliveryIsTearDown() {
        return localDelivery.getStatus().equals(LocalDeliveryStatus.TEAR_DOWN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryCapabilityMatch that = (DeliveryCapabilityMatch) o;
        return localDelivery.equals(that.localDelivery) && capability.equals(that.capability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localDelivery, capability);
    }

    @Override
    public String toString() {
        return "DeliveryCapabilityMatch{" +
                "localDelivery=" + localDelivery +
                ", capability=" + capability +
                '}';
    }
}
