package no.vegvesen.ixn.docker.keygen;

public class ServicProviderDescription {
    private String name;
    private String country;

    public ServicProviderDescription() {

    }

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
