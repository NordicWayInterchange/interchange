package no.vegvesen.ixn.federation.adminserver.model;

import java.util.Set;

public class InterchangeApi {

    private Set<InterchangeSubscriptionApi> subscriptions;

    private Set<InterchangeCapabilityApi> capabilities;

    private Set<InterchangeDeliveryApi> deliveries;


    public InterchangeApi(
            Set<InterchangeSubscriptionApi> subscriptions,
            Set<InterchangeCapabilityApi> capabilities,
            Set<InterchangeDeliveryApi> deliveries) {
        this.subscriptions = subscriptions;
        this.capabilities = capabilities;
        this.deliveries = deliveries;
    }

    public InterchangeApi() {

    }

    public Set<InterchangeSubscriptionApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<InterchangeSubscriptionApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<InterchangeCapabilityApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<InterchangeCapabilityApi> capabilities) {
        this.capabilities = capabilities;
    }

    public Set<InterchangeDeliveryApi> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<InterchangeDeliveryApi> deliveries) {
        this.deliveries = deliveries;
    }

    @Override
    public String toString() {
        return "InterchangeApi{" +
                ", capabilities=" + capabilities +
                ", subscriptions=" + subscriptions +
                ", deliveries=" + deliveries +
                '}';
    }
}
