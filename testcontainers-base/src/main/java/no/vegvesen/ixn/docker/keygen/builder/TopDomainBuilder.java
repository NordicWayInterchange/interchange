package no.vegvesen.ixn.docker.keygen.builder;

import no.vegvesen.ixn.docker.keygen.IntermediateDomain;
import no.vegvesen.ixn.docker.keygen.TopDomain;

import java.util.ArrayList;
import java.util.List;

public class TopDomainBuilder {
    private final ClusterBuilder parent;
    private String domainName;
    private String ownerCountry;

    private List<IntermediateDomain> intermediateDomains = new ArrayList<>();

    public TopDomainBuilder(ClusterBuilder parent) {
        this.parent = parent;
    }


    public TopDomainBuilder domainName(String domainName) {
        this.domainName = domainName;
        return this;
    }

    public TopDomainBuilder ownerCountry(String countryCode) {
        this.ownerCountry = countryCode;
        return this;
    }

    public IntermediateDomainBuilder intermediateDomain() {
        return new IntermediateDomainBuilder(this);
    }

    public TopDomainBuilder addIntermediateDomain(IntermediateDomain domain) {
        intermediateDomains.add(domain);
        return this;
    }

    public ClusterBuilder done() {
        TopDomain topDomain = new TopDomain(domainName, ownerCountry,intermediateDomains);
        parent.addTopDomain(topDomain);
        return parent;
    }


}
