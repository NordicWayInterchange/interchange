package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;
import java.util.Set;

public class ServiceProviderExportApi {

    private String name;

    private Set<LocalSubscriptionExportApi> subscriptions;

    private Set<CapabilityExportApi> capabilities;

    private Set<DeliveryExportApi> deliveries;

    public ServiceProviderExportApi() {

    }

    public ServiceProviderExportApi(String name,
                                    Set<LocalSubscriptionExportApi> subscriptions,
                                    Set<CapabilityExportApi> capabilities,
                                    Set<DeliveryExportApi> deliveries) {
        this.name = name;
        this.subscriptions = subscriptions;
        this.capabilities = capabilities;
        this.deliveries = deliveries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<LocalSubscriptionExportApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<LocalSubscriptionExportApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<CapabilityExportApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilityExportApi> capabilities) {
        this.capabilities = capabilities;
    }

    public Set<DeliveryExportApi> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<DeliveryExportApi> deliveries) {
        this.deliveries = deliveries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceProviderExportApi that = (ServiceProviderExportApi) o;
        return Objects.equals(name, that.name) && Objects.equals(subscriptions, that.subscriptions) && Objects.equals(capabilities, that.capabilities) && Objects.equals(deliveries, that.deliveries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, subscriptions, capabilities, deliveries);
    }

    @Override
    public String toString() {
        return "ServiceProviderExportApi{" +
                "name='" + name + '\'' +
                ", subscriptions=" + subscriptions +
                ", capabilities=" + capabilities +
                ", deliveries=" + deliveries +
                '}';
    }
}

