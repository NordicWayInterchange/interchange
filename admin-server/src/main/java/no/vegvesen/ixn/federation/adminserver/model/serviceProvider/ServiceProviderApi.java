package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import java.util.Set;

public class ServiceProviderApi {

    private String name;

    private Set<LocalSubscriptionApi> subscriptions;

    private Set<CapabilityApi> capabilities;

    private Set<LocalDeliveryApi> deliveries;


    public ServiceProviderApi(
            String name,
            Set<LocalSubscriptionApi> subscriptions,
            Set<CapabilityApi> capabilities,
            Set<LocalDeliveryApi> deliveries) {
        this.name = name;
        this.subscriptions = subscriptions;
        this.capabilities = capabilities;
        this.deliveries = deliveries;
    }

    public ServiceProviderApi() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<LocalSubscriptionApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<LocalSubscriptionApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<CapabilityApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilityApi> capabilities) {
        this.capabilities = capabilities;
    }

    public Set<LocalDeliveryApi> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<LocalDeliveryApi> deliveries) {
        this.deliveries = deliveries;
    }

    @Override
    public String toString() {
        return "serviceProviderApi{" +
                "name='" + name + '\'' +
                ", capabilities=" + capabilities +
                ", subscriptions=" + subscriptions +
                ", deliveries=" + deliveries +
                '}';
    }
}
