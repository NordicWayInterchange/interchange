package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NeighbourFetcher {
    private InterchangeRepository repository;


    @Autowired
    public NeighbourFetcher(InterchangeRepository repository) {
        this.repository = repository;
    }

    public List<Interchange> listNeighbourCandidates() {
        List<Interchange> interchanges = repository.findInterchangesForMessageForwarding();
        return interchanges;

    }
}
