package no.vegvesen.ixn.federation.service.exportmodel;

public class PeerApi {
    private final String name;

    public PeerApi(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
