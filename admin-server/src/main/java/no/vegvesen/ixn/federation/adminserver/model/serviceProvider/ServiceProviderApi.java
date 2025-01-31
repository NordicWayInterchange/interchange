package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import java.util.List;
import java.util.Set;

public class ServiceProviderApi {

    private Integer id;
    private String name;

    private List<LocalSubscriptionApi> subscriptions;

    private Set<CapabilityApi> capabilities;

    private Set<LocalDeliveryApi> deliveries;


    public ServiceProviderApi(
            Integer id,
            String name,
            List<LocalSubscriptionApi> subscriptions,
            Set<CapabilityApi> capabilities,
            Set<LocalDeliveryApi> deliveries) {
        this.id = id;
        this.name = name;
        this.subscriptions = subscriptions;
        this.capabilities = capabilities;
        this.deliveries = deliveries;
    }

    public ServiceProviderApi() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LocalSubscriptionApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<LocalSubscriptionApi> subscriptions) {
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
                "id=" + id +
                "name='" + name + '\'' +
                ", capabilities=" + capabilities +
                ", subscriptions=" + subscriptions +
                ", deliveries=" + deliveries +
                '}';
    }
}
