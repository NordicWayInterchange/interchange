package no.vegvesen.ixn.federation.dbhelper;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@ConditionalOnProperty(name = "db-helper.type", havingValue = "message-forwarder")
public class MessageForwarderSystemTestDataLoader implements DatabaseHelperInterface {


    private NeighbourRepository neighbourRepository;

    @Autowired
    public MessageForwarderSystemTestDataLoader(NeighbourRepository repository) {
        this.neighbourRepository = repository;
    }


    @Override
    public void fillDatabase() {
        SubscriptionRequest outgoing = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED, Collections.EMPTY_SET);
        Neighbour remote = new Neighbour("remote",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                outgoing,
                new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED, Collections.EMPTY_SET));
        remote.setMessageChannelPort("5671");
        neighbourRepository.save(remote);

    }
}
