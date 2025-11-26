package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import java.util.List;


public class ServiceProviderApi implements Comparable<ServiceProviderApi>{

    private Integer id;

    private String name;

    private Boolean biconsumer;

    private List<LocalSubscriptionApi> subscriptions;

    private List<CapabilityApi> capabilities;

    private List<LocalDeliveryApi> deliveries;

    private BiqueueEndpointApi biqueueEndpoint;

    public ServiceProviderApi(
            Integer id,
            String name,
            Boolean biconsumer,
            BiqueueEndpointApi biqueueEndpoint,
            List<LocalSubscriptionApi> subscriptions,
            List<CapabilityApi> capabilities,
            List<LocalDeliveryApi> deliveries) {
        this.id = id;
        this.name = name;
        this.biconsumer = biconsumer;
        this.biqueueEndpoint = biqueueEndpoint;
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

    public Boolean getBiconsumer() {
        return biconsumer;
    }

    public void setBiconsumer(Boolean biconsumer) {
        this.biconsumer = biconsumer;
    }

    public List<LocalSubscriptionApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<LocalSubscriptionApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<CapabilityApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<CapabilityApi> capabilities) {
        this.capabilities = capabilities;
    }

    public List<LocalDeliveryApi> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<LocalDeliveryApi> deliveries) {
        this.deliveries = deliveries;
    }

    public BiqueueEndpointApi getBiqueueEndpoint() {
        return biqueueEndpoint;
    }

    public void setBiqueueEndpoint(BiqueueEndpointApi biqueueEndpoint) {
        this.biqueueEndpoint = biqueueEndpoint;
    }

    @Override
    public int compareTo(ServiceProviderApi serviceProviderApi) {
        if(id == null && serviceProviderApi.id == null) {
            return 0;
        }

        if(serviceProviderApi.id == null){
            return 1;
        }

        if(id == null) {
            return -1;
        }
        return Long.compare(id, serviceProviderApi.id);
    }

    @Override
    public String toString() {
        return "serviceProviderApi{" +
                "id=" + id +
                "name='" + name + '\'' +
                ", biconsumer=" + biconsumer +
                ", biqueueEndpoint=" + biqueueEndpoint +
                ", capabilities=" + capabilities +
                ", subscriptions=" + subscriptions +
                ", deliveries=" + deliveries +
                '}';
    }
}
