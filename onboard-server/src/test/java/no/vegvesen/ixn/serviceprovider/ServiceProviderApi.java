package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.serviceprovider.model.GetDeliveryResponse;
import no.vegvesen.ixn.serviceprovider.model.GetSubscriptionResponse;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelResponseApi;

import java.util.Set;

public class ServiceProviderApi {
    private String name;
    private Set<GetSubscriptionResponse> subscriptions;
    private Set<CapabilityApi> capabilities;

    private Set<GetDeliveryResponse> deliveries;

    private Set<PrivateChannelResponseApi> privateChannels;

    public Set<GetSubscriptionResponse> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<GetSubscriptionResponse> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<CapabilityApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilityApi> capabilities) {
        this.capabilities = capabilities;
    }


    public Set<GetDeliveryResponse> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<GetDeliveryResponse> deliveries) {
        this.deliveries = deliveries;
    }

    public Set<PrivateChannelResponseApi> getPrivateChannels() {
        return privateChannels;
    }

    public void setPrivateChannels(Set<PrivateChannelResponseApi> privateChannels) {
        this.privateChannels = privateChannels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
