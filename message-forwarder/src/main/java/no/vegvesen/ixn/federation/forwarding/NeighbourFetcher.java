package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NeighbourFetcher {
    private NeighbourRepository repository;


    @Autowired
    public NeighbourFetcher(NeighbourRepository repository) {
        this.repository = repository;
    }

    public List<Neighbour> listNeighbourCandidates() {
        List<Neighbour> interchanges = repository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
        return interchanges;
    }
}
