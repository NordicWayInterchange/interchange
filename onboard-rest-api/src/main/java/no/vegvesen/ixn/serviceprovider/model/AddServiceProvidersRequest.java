package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.federation.model.ServiceProvider;

import java.util.Set;

public class AddServiceProvidersRequest {
    private String version = "1.0";
    private Set<AddServiceProviders> serviceProviders;

    public AddServiceProvidersRequest() {
    }

    public AddServiceProvidersRequest(String version, Set<AddServiceProviders> serviceProviders) {
        this.version = version;
        this.serviceProviders = serviceProviders;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<AddServiceProviders> getServiceProviders() {
        return serviceProviders;
    }

    public void setServiceProviders(Set<AddServiceProviders> serviceProviders) {
        this.serviceProviders = serviceProviders;
    }
}
