package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("WeakerAccess")
@Component
public class NeighbourFetcher {
    private NeighbourRepository repository;


    @Autowired
    public NeighbourFetcher(NeighbourRepository repository) {
        this.repository = repository;
    }

}
