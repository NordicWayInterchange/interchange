package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;

import java.util.Set;

public class AddServiceProviders {
    private String name;
    private Set<CapabilityApi> capabilities;
    private Set<String> subscriptions;
    private Set<String> deliveries;

    public AddServiceProviders(){
        
    }

    public AddServiceProviders(String name, Set<CapabilityApi> capabilities, Set<String> subscriptions, Set<String> deliveries) {
        this.name = name;
        this.capabilities = capabilities;
        this.subscriptions = subscriptions;
        this.deliveries = deliveries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<CapabilityApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilityApi> capabilities) {
        this.capabilities = capabilities;
    }

    public Set<String> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<String> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<String> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<String> deliveries) {
        this.deliveries = deliveries;
    }
}
