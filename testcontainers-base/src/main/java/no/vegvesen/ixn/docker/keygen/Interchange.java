package no.vegvesen.ixn.docker.keygen;

import java.util.List;

public class Interchange {


    private final IntermediateDomain domain;
    private final List<AdditionalHost> additionalHosts;
    private final List<ServicProviderDescription> serviceProviders;

    public Interchange(IntermediateDomain domain, List<AdditionalHost> additionalHosts, List<ServicProviderDescription> serviceProviders) {
        this.domain = domain;
        this.additionalHosts = additionalHosts;
        this.serviceProviders = serviceProviders;
    }

    public IntermediateDomain getDomain() {
        return domain;
    }

    public List<AdditionalHost> getAdditionalHosts() {
        return additionalHosts;
    }

    public List<ServicProviderDescription> getServiceProviders() {
        return serviceProviders;
    }
}
