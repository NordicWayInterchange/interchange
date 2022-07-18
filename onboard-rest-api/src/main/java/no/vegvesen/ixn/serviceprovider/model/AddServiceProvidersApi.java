package no.vegvesen.ixn.serviceprovider.model;

import java.util.Set;

public class AddServiceProvidersApi {
    private String name;
    private Set<LocalActorCapability> capabilities;
    private Set<LocalActorSubscription> subscriptions;
    private Set<Delivery> deliveries;


    public AddServiceProvidersApi() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<LocalActorCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<LocalActorCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public Set<LocalActorSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<LocalActorSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<Delivery> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<Delivery> deliveries) {
        this.deliveries = deliveries;
    }

}
