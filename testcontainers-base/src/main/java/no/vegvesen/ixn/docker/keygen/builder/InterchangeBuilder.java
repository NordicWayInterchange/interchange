package no.vegvesen.ixn.docker.keygen.builder;

import no.vegvesen.ixn.docker.keygen.AdditionalHost;
import no.vegvesen.ixn.docker.keygen.Interchange;
import no.vegvesen.ixn.docker.keygen.IntermediateDomain;
import no.vegvesen.ixn.docker.keygen.ServicProviderDescription;

import java.util.ArrayList;
import java.util.List;

public class InterchangeBuilder {
    private final ClusterBuilder parent;
    private IntermediateDomain domain;
    private List<AdditionalHost> additionalHosts = new ArrayList<>();
    private List<ServicProviderDescription> serviceProviders = new ArrayList<>();

    public InterchangeBuilder(ClusterBuilder parent) {
        this.parent = parent;
    }

    public ClusterBuilder done() {
        parent.addInterchange(new Interchange(domain, additionalHosts, serviceProviders));
        return parent;
    }

    public AdditionalHostBuilder additionalHost() {
        return new AdditionalHostBuilder(this);
    }

    public IntermediateDomainBuilder intermediateDomain() {
        return new IntermediateDomainBuilder(this);
    }

    public void setIntermediateDomain(IntermediateDomain intermediateDomain) {
        this.domain = intermediateDomain;

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
