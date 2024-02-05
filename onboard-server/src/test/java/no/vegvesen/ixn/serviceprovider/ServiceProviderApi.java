package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.serviceprovider.model.GetDeliveryResponse;
import no.vegvesen.ixn.serviceprovider.model.GetSubscriptionResponse;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelApi;

import java.util.Set;

public class ServiceProviderApi {
    private String name;
    private Set<GetSubscriptionResponse> subscriptions;
    private Set<CapabilitySplitApi> capabilities;

    private Set<GetDeliveryResponse> deliveries;

    public ServiceProviderApi() {

    }

    public ServiceProviderApi(String name, Set<GetSubscriptionResponse> subscriptions, Set<CapabilitySplitApi> capabilities, Set<GetDeliveryResponse> deliveries) {
        this.name = name;
        this.subscriptions = subscriptions;
        this.capabilities  = capabilities;
        this.deliveries = deliveries;
    }

    public Set<GetSubscriptionResponse> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<GetSubscriptionResponse> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<CapabilitySplitApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilitySplitApi> capabilities) {
        this.capabilities = capabilities;
    }


    public Set<GetDeliveryResponse> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<GetDeliveryResponse> deliveries) {
        this.deliveries = deliveries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
