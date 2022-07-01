import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;

import java.util.List;
import java.util.Set;

public class getAllNeighboursResponse {

    private List<Neighbour> neighbours;

    public getAllNeighboursResponse(NeighbourRepository neighbourRepository){

        this.neighbours = neighbourRepository.findAll();

    }

    public List<Neighbour> getNeighbours() {
        return neighbours;
    }
}
