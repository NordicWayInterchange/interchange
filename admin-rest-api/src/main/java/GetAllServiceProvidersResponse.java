import no.vegvesen.ixn.federation.model.ServiceProvider;

import java.util.List;

public class GetAllServiceProvidersResponse {

    private List<ServiceProvider> serviceProviders;

    public GetAllServiceProvidersResponse(List<ServiceProvider> serviceProviders){
        this.serviceProviders = serviceProviders;
    }

    public List<ServiceProvider> getServiceProviders() {
        return serviceProviders;
    }

}
