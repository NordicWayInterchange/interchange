package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.serviceprovider.model.OldPrivateChannelApi;

import java.util.Set;

public class OldServiceProviderApi {
    private String name;
    private Set<OldLocalActorSubscription> subscriptions;
    private Set<CapabilitySplitApi> capabilities;
    private Set<DeliveryApi> deliveries;
    private Set<OldPrivateChannelApi> privateChannels;

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

    public Set<CapabilitySplitApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilitySplitApi> capabilities) {
        this.capabilities = capabilities;
    }

    public Set<DeliveryApi> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<DeliveryApi> deliveries) {
        this.deliveries = deliveries;
    }

    public Set<OldPrivateChannelApi> getPrivateChannels() {
        return privateChannels;
    }

    public void setPrivateChannels(Set<OldPrivateChannelApi> privateChannels) {
        this.privateChannels = privateChannels;
    }

    @Override
    public String toString() {
        return "OldServiceProviderApi{" +
                "name='" + name + '\'' +
                ", subscriptions=" + subscriptions +
                ", capabilities=" + capabilities +
                ", deliveries=" + deliveries +
                ", privateChannels=" + privateChannels +
                '}';
    }
}
