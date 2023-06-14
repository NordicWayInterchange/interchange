package no.vegvesen.ixn.docker.keygen;

import java.util.Collections;
import java.util.List;

public class TopDomain {
    private String domainName;
    private String ownerCountry;

    private List<IntermediateDomain> intermediateDomains;

    public TopDomain() {

    }

    public TopDomain(String domainName, String ownerCountry) {
        this.domainName = domainName;
        this.ownerCountry = ownerCountry;
        this.intermediateDomains = Collections.emptyList();
    }
    public TopDomain(String domainName, String ownerCountry, List<IntermediateDomain> intermediateDomains) {
        this.domainName = domainName;
        this.ownerCountry = ownerCountry;
        this.intermediateDomains = intermediateDomains;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getOwnerCountry() {
        return ownerCountry;
    }

    public List<IntermediateDomain> getIntermediateDomains() {
        return intermediateDomains;
    }
}
