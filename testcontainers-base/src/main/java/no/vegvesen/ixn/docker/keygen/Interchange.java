package no.vegvesen.ixn.docker.keygen;

import java.util.List;

public class Interchange {


    private List<AdditionalHost> additionalHosts;
    private List<ServicProviderDescription> serviceProviders;

    public Interchange() {

    }

    public Interchange(List<AdditionalHost> additionalHosts, List<ServicProviderDescription> serviceProviders) {
        this.additionalHosts = additionalHosts;
        this.serviceProviders = serviceProviders;
    }

    public List<AdditionalHost> getAdditionalHosts() {
        return additionalHosts;
    }

    public List<ServicProviderDescription> getServiceProviders() {
        return serviceProviders;
    }
}
