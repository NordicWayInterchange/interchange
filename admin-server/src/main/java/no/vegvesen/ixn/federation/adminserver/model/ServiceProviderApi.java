package no.vegvesen.ixn.federation.adminserver.model;

import java.util.Set;

public class ServiceProviderApi {

    private Set<ServiceProviderSubscriptionApi> subscriptions;

    private ServiceProviderCapabilityApi capabilities;

    private Set<ServiceProviderDeliveryApi> deliveries;


    public ServiceProviderApi(
            Set<ServiceProviderSubscriptionApi> subscriptions,
            ServiceProviderCapabilityApi capabilities,
            Set<ServiceProviderDeliveryApi> deliveries) {
        this.subscriptions = subscriptions;
        this.capabilities = capabilities;
        this.deliveries = deliveries;
    }

    public ServiceProviderApi() {

    }

    public Set<ServiceProviderSubscriptionApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<ServiceProviderSubscriptionApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public ServiceProviderCapabilityApi getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(ServiceProviderCapabilityApi capabilities) {
        this.capabilities = capabilities;
    }

    public Set<ServiceProviderDeliveryApi> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<ServiceProviderDeliveryApi> deliveries) {
        this.deliveries = deliveries;
    }

    @Override
    public String toString() {
        return "serviceProviderApi{" +
                ", capabilities=" + capabilities +
                ", subscriptions=" + subscriptions +
                ", deliveries=" + deliveries +
                '}';
    }
}
