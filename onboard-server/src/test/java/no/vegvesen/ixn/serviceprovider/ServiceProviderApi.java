package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.serviceprovider.model.LocalActorSubscription;

import java.util.Set;

public class ServiceProviderApi {
    private String name;
    private Set<LocalActorSubscription> subscriptions;
    private Set<CapabilityApi> capabilities;

    public Set<LocalActorSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<LocalActorSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<CapabilityApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilityApi> capabilities) {
        this.capabilities = capabilities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}