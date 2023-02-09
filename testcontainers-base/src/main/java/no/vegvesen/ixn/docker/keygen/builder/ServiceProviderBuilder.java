package no.vegvesen.ixn.docker.keygen.builder;

import no.vegvesen.ixn.docker.keygen.ServicProviderDescription;

public class ServiceProviderBuilder {
    private final InterchangeBuilder parent;
    private String name;
    private String country;

    public ServiceProviderBuilder(InterchangeBuilder parent) {
        this.parent = parent;
    }

    public ServiceProviderBuilder name(String serviceProviderName) {
        this.name = serviceProviderName;
        return this;
    }

    public ServiceProviderBuilder country(String country) {
        this.country = country;
        return this;
    }

    public InterchangeBuilder done() {
        parent.addServiceProvider(new ServicProviderDescription(name, country));
        return parent;
    }

}
