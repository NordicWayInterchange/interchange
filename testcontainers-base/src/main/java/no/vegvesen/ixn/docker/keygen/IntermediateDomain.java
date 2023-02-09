package no.vegvesen.ixn.docker.keygen;

public class IntermediateDomain {
    private final String domainName;
    private final String owningCountry;

    public IntermediateDomain(String domainName, String owningCountry) {
        this.domainName = domainName;
        this.owningCountry = owningCountry;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getOwningCountry() {
        return owningCountry;
    }
}
