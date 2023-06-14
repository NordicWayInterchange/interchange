package no.vegvesen.ixn.docker.keygen.builder;

import no.vegvesen.ixn.docker.keygen.Interchange;
import no.vegvesen.ixn.docker.keygen.IntermediateDomain;

public class IntermediateDomainBuilder {

    private final TopDomainBuilder parent;
    private String domainName;
    private String owningCountry;

    Interchange interchange;

    public IntermediateDomainBuilder(TopDomainBuilder parent) {
        this.parent = parent;
    }

    public IntermediateDomainBuilder domainName(String domainName) {
        this.domainName = domainName;
        return this;
    }

    public IntermediateDomainBuilder ownerCountry(String owningCountry) {
        this.owningCountry = owningCountry;
        return this;
    }

    public InterchangeBuilder interchange() {
        return new InterchangeBuilder(this);
    }

    public IntermediateDomainBuilder addInterchange(Interchange interchange) {
        this.interchange = interchange;
        return this;
    }

    public TopDomainBuilder done() {
        return parent.addIntermediateDomain(new IntermediateDomain(domainName,owningCountry,interchange));
    }

}
