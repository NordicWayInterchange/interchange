package no.vegvesen.ixn.docker.keygen.builder;

import no.vegvesen.ixn.docker.keygen.TopDomain;

public class TopDomainBuilder {
    private final ClusterBuilder parent;
    private String domainName;
    private String ownerCountry;

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

    public ClusterBuilder done() {
        TopDomain topDomain = new TopDomain(domainName, ownerCountry);
        parent.addTopDomain(topDomain);
        return parent;
    }


}
