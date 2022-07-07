import no.vegvesen.ixn.federation.model.ServiceProvider;

import java.util.List;

public class GetAllServiceProvidersResponse {

    private String nameOfInterchange;
    private List<ServiceProvider> serviceProviders;

    public GetAllServiceProvidersResponse(String nameOfInterchange, List<ServiceProvider> serviceProviders){
        this.nameOfInterchange = nameOfInterchange;
        this.serviceProviders = serviceProviders;
    }

    public List<ServiceProvider> getServiceProviders() {
        return serviceProviders;
    }

}
