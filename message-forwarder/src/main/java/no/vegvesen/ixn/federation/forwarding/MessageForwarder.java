package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.federation.model.Interchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class MessageForwarder {

    private NeighbourFetcher neighbourFetcher;

    //Need some private list of connections.. TODO this will probably not work ina a threaded environment...
    private List<Interchange> connectedInterchanges;
    private Logger logger = LoggerFactory.getLogger(MessageForwarder.class);


    @Autowired
    public MessageForwarder(NeighbourFetcher fetcher) {
        this.neighbourFetcher = fetcher;
        this.connectedInterchanges = new ArrayList<>();
    }

    //Call the rest api and get the list of queues
    //Or, use the database to get the neighbours, then check if I already have a connection
    //This should be scheduled(?)
    public void setupConnectionsToNewNeighbours() {
        List<Interchange> interchanges = neighbourFetcher.listNeighbourCandidates();
        for (Interchange ixn : interchanges) {
            System.out.println(String.format("name: %s, address %s:%s, fedIn: %s status: %s",ixn.getName(),ixn.getDomainName(),ixn.getControlChannelPort(),ixn.getFedIn(),ixn.getInterchangeStatus()));
            if (! connectedInterchanges.contains(ixn)) {
                System.out.println(String.format("Connecting to %s",ixn.getDomainName()));
                connectedInterchanges.add(ixn);
            }
        }
    }

}
