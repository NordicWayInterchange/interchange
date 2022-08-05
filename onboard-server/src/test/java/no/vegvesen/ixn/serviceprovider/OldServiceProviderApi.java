package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;

import java.util.Set;

public class OldServiceProviderApi {
    private String name;
    private Set<OldLocalActorSubscription> subscriptions;
    private Set<CapabilityApi> capabilities;

    private Set<DeliveryApi> deliveries;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<OldLocalActorSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<OldLocalActorSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<CapabilityApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilityApi> capabilities) {
        this.capabilities = capabilities;
    }

    public Set<DeliveryApi> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<DeliveryApi> deliveries) {
        this.deliveries = deliveries;
    }

    @Override
    public String toString() {
        return "OldServiceProviderApi{" +
                "name='" + name + '\'' +
                ", subscriptions=" + subscriptions +
                ", capabilities=" + capabilities +
                ", deliveries=" + deliveries +
                '}';
    }
}
