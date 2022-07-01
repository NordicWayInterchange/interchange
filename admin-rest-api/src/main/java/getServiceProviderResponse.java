import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.serviceprovider.model.Delivery;
import no.vegvesen.ixn.serviceprovider.model.LocalActorCapability;

import java.util.Set;

public class getServiceProviderResponse {

    private String name;

    private ServiceProvider serviceProvider;

    private Capabilities capabilities;

    private Set<LocalSubscription> subscriptions;

    private Set<LocalDelivery> deliveries;
    private int id;


    public getServiceProviderResponse(ServiceProvider serviceProvider){

        this.serviceProvider = serviceProvider;
        this.name = serviceProvider.getName();
        this.capabilities = serviceProvider.getCapabilities();
        this.id = serviceProvider.getId();
        this.subscriptions = serviceProvider.getSubscriptions();
        this.deliveries = serviceProvider.getDeliveries();

    }



    public String getName() {
        return name;
    }

    public Capabilities getCapabilities() {
        return capabilities;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public int getId() {
        return id;
    }

    public Set<LocalDelivery> getDeliveries() {
        return deliveries;
    }

    public Set<LocalSubscription> getSubscriptions() {
        return subscriptions;
    }



}


