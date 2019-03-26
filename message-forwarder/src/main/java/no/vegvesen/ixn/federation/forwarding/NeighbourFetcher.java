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

    //TODO this is not yet enabled
    //@Scheduled(fixedRate = 15000, initialDelay = 3000)  // check every 15 seconds. 5 second delay from start.
    public List<Interchange> listNeighbourCandidates() {
        List<Interchange> interchanges = repository.findFederatedInterchanges();
        //for (Interchange interchange : interchanges) {
        //    System.out.println(String.format("%s:%s",interchange.getName(),interchange.getControlChannelPort()));
        //}
        return interchanges;

    }
}
