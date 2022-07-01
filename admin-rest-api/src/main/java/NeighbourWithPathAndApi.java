import no.vegvesen.ixn.federation.api.v1_0.NeighbourApi;

public class NeighbourWithPathAndApi {
    private String id;
    private String path;
    private NeighbourApi definition;

    public NeighbourWithPathAndApi(String id, String path, NeighbourApi definition) {
        this.id = id;
        this.path = path;
        this.definition = definition;
    }
}
