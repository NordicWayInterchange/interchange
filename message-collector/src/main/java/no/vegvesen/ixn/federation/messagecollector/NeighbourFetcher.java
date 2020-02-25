package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("WeakerAccess")
@Component
public class NeighbourFetcher {
    private NeighbourRepository repository;


    @Autowired
    public NeighbourFetcher(NeighbourRepository repository) {
        this.repository = repository;
    }

    /*
    public List<Neighbour> listNeighbourCandidates() {
		return repository.findBySubscriptionRequest_Status(SubscriptionRequestStatus.ESTABLISHED);
    }
*/
    public List<Neighbour> listNeighboursToConsumeFrom() {
        List<Neighbour> neighbours = repository.findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
        return neighbours;
    }
}
