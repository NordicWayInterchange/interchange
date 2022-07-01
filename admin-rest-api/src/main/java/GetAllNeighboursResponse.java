import no.vegvesen.ixn.federation.api.v1_0.NeighbourApi;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;

import java.util.List;
import java.util.Set;

public class GetAllNeighboursResponse {

    private Set<NeighbourWithPathAndApi> neighbours;

    public GetAllNeighboursResponse(Set<NeighbourWithPathAndApi> neighbours){

        this.neighbours = neighbours;

    }

}
