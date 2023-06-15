package no.vegvesen.ixn.docker.keygen.builder;

import no.vegvesen.ixn.docker.keygen.*;

import java.util.ArrayList;
import java.util.List;

public class InterchangeBuilder {
    private final IntermediateDomainBuilder parent;
    private List<AdditionalHost> additionalHosts = new ArrayList<>();
    private List<ServicProviderDescription> serviceProviders = new ArrayList<>();

    public InterchangeBuilder(IntermediateDomainBuilder parent) {
        this.parent = parent;
    }

    public IntermediateDomainBuilder done() {
        return parent.addInterchange(new Interchange(additionalHosts, serviceProviders));
    }

    public AdditionalHostBuilder additionalHost() {
        return new AdditionalHostBuilder(this);
    }


    public ServiceProviderBuilder serviceProvider() {
        return new ServiceProviderBuilder(this);
    }

    public void addServiceProvider(ServicProviderDescription sp) {
        serviceProviders.add(sp);
    }

    public void addAdditionalHost(AdditionalHost additionalHost) {
        additionalHosts.add(additionalHost);
    }
}
