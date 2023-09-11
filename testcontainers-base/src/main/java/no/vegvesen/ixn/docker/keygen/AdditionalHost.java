package no.vegvesen.ixn.docker.keygen;

public class AdditionalHost {
    private String hostname;
    private String owningCountry;

    public AdditionalHost() {

    }
    public AdditionalHost(String hostname, String owningCountry) {
        this.hostname = hostname;
        this.owningCountry = owningCountry;
    }

    public String getHostname() {
        return hostname;
    }

    public String getOwningCountry() {
        return owningCountry;
    }
}
