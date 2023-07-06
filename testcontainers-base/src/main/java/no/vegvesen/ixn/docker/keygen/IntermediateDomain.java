package no.vegvesen.ixn.docker.keygen;


public class IntermediateDomain {
    private String domainName;
    private String owningCountry;

    private Interchange interchange;

    public IntermediateDomain() {

    }

    public IntermediateDomain(String domainName, String owningCountry, Interchange interchange) {
        this.domainName = domainName;
        this.owningCountry = owningCountry;
        this.interchange = interchange;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getOwningCountry() {
        return owningCountry;
    }

    public Interchange getInterchange() {
        return interchange;
    }
}
