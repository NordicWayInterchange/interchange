package no.vegvesen.ixn.docker.keygen;

public class ServicProviderDescription {
    private final String name;
    private final String country;

    public ServicProviderDescription(String name, String country) {
        this.name = name;
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }
}
