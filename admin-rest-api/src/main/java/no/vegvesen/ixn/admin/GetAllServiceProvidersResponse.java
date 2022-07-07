package no.vegvesen.ixn.admin;

import java.util.Set;

public class GetAllServiceProvidersResponse {

    private String nameOfInterchange;
    private Set<ServiceProviderApi> serviceProviders;

    public GetAllServiceProvidersResponse(String nameOfInterchange, Set<ServiceProviderApi> serviceProviders){
        this.nameOfInterchange = nameOfInterchange;
        this.serviceProviders = serviceProviders;
    }

    public Set<ServiceProviderApi> getServiceProviders() {
        return serviceProviders;
    }

    public String getNameOfInterchange() {
        return nameOfInterchange;
    }

    public void setNameOfInterchange(String nameOfInterchange) {
        this.nameOfInterchange = nameOfInterchange;
    }

    public void setServiceProviders(Set<ServiceProviderApi> serviceProviders) {
        this.serviceProviders = serviceProviders;
    }
}
