package no.vegvesen.ixn.docker.keygen;

public class TopDomain {
    private final String domainName;
    private final String ownerCountry;

    public TopDomain(String domainName, String ownerCountry) {
        this.domainName = domainName;
        this.ownerCountry = ownerCountry;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getOwnerCountry() {
        return ownerCountry;
    }
}
