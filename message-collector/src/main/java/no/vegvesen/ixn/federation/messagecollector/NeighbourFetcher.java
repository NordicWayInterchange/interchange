package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.Neighbour;
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

    public List<Neighbour> listNeighboursToConsumeFrom() {
		return repository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus.CREATED);
    }
}
