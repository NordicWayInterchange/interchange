package no.vegvesen.ixn.docker.keygen.builder;

import no.vegvesen.ixn.docker.KeyGenerationBuilderIT;
import no.vegvesen.ixn.docker.keygen.AdditionalHost;

public class AdditionalHostBuilder {
    private final InterchangeBuilder parent;
    private String hostname;
    private String owningCountry;


    public AdditionalHostBuilder(InterchangeBuilder interchangeBuilder) {
        this.parent = interchangeBuilder;
    }

    public AdditionalHostBuilder hostName(String domainName) {
        this.hostname = domainName;
        return this;
    }

    public InterchangeBuilder done() {
        parent.addAdditionalHost(new AdditionalHost(hostname, owningCountry));
        return parent;
    }

    public AdditionalHostBuilder ownerCountry(String ownerCountry) {
        this.owningCountry = ownerCountry;
        return this;
    }
}
