package no.vegvesen.ixn.federation.service.importmodel;

import java.util.Objects;
import java.util.Set;

public class ServiceProviderImportApi {

    private String name;

    private Set<LocalSubscriptionImportApi> subscriptions;

    private Set<CapabilityImportApi> capabilities;

    private Set<DeliveryImportApi> deliveries;

    private long subscriptionsUpdated;

    public ServiceProviderImportApi() {

    }

    public ServiceProviderImportApi(String name,
                                    Set<LocalSubscriptionImportApi> subscriptions,
                                    Set<CapabilityImportApi> capabilities,
                                    Set<DeliveryImportApi> deliveries,
                                    long subscriptionsUpdated) {
        this.name = name;
        this.subscriptions = subscriptions;
        this.capabilities = capabilities;
        this.deliveries = deliveries;
        this.subscriptionsUpdated = subscriptionsUpdated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<LocalSubscriptionImportApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<LocalSubscriptionImportApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<CapabilityImportApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilityImportApi> capabilities) {
        this.capabilities = capabilities;
    }

    public Set<DeliveryImportApi> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<DeliveryImportApi> deliveries) {
        this.deliveries = deliveries;
    }

    public long getSubscriptionsUpdated() {
        return subscriptionsUpdated;
    }

    public void setSubscriptionsUpdated(long subscriptionsUpdated) {
        this.subscriptionsUpdated = subscriptionsUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceProviderImportApi that = (ServiceProviderImportApi) o;
        return subscriptionsUpdated == that.subscriptionsUpdated && Objects.equals(name, that.name) && Objects.equals(subscriptions, that.subscriptions) && Objects.equals(capabilities, that.capabilities) && Objects.equals(deliveries, that.deliveries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, subscriptions, capabilities, deliveries, subscriptionsUpdated);
    }

    @Override
    public String toString() {
        return "ServiceProviderImportApi{" +
                "name='" + name + '\'' +
                ", subscriptions=" + subscriptions +
                ", capabilities=" + capabilities +
                ", deliveries=" + deliveries +
                ", subscriptionsUpdated=" + subscriptionsUpdated +
                '}';
    }
}
