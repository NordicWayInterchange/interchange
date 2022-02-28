package no.vegvesen.ixn.docker.keygen.builder;

import no.vegvesen.ixn.docker.KeyGenerationBuilderIT;
import no.vegvesen.ixn.docker.keygen.IntermediateDomain;

public class IntermediateDomainBuilder {

    private final InterchangeBuilder interchangeBuilder;
    private String domainName;
    private String owningCountry;

    public IntermediateDomainBuilder(InterchangeBuilder interchangeBuilder) {

        this.interchangeBuilder = interchangeBuilder;
    }

    public IntermediateDomainBuilder domainName(String domainName) {
        this.domainName = domainName;
        return this;
    }

    public IntermediateDomainBuilder ownerCountry(String owningCountry) {
        this.owningCountry = owningCountry;
        return this;
    }

    public InterchangeBuilder done() {
        interchangeBuilder.setIntermediateDomain(new IntermediateDomain(domainName, owningCountry));
        return interchangeBuilder;
    }

}
