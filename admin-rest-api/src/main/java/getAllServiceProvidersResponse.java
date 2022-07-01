import no.vegvesen.ixn.federation.model.ServiceProvider;

import java.util.List;
import java.util.Set;

public class getAllServiceProvidersResponse {

    private List<ServiceProvider> serviceProviders;

    public getAllServiceProvidersResponse(List<ServiceProvider> serviceProviders){
        this.serviceProviders = serviceProviders;
    }

    public List<ServiceProvider> getServiceProviders() {
        return serviceProviders;
    }

}
