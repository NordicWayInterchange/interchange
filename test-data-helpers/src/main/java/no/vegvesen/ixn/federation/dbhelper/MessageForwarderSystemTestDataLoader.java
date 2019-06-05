package no.vegvesen.ixn.federation.dbhelper;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class MessageForwarderSystemTestDataLoader implements DatabaseHelperInterface {


    private InterchangeRepository interchangeRepository;

    @Autowired
    public MessageForwarderSystemTestDataLoader(InterchangeRepository repository) {
        this.interchangeRepository = repository;
    }


    @Override
    public void fillDatabase() {
        SubscriptionRequest outgoing = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED, Collections.EMPTY_SET);
        Interchange remote = new Interchange("remote",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                outgoing,
                new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED, Collections.EMPTY_SET));
        remote.setMessageChannelPort("5671");
        interchangeRepository.save(remote);

    }
}
