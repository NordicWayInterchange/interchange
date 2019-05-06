package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NeighbourFetcherIT {

    @Autowired
    private InterchangeRepository repository;

    @Before
    public void setUp() {
        repository.save(new Interchange("Intermittent Interchange",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED,Collections.EMPTY_SET),
                new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED,Collections.EMPTY_SET)));
        repository.save(new Interchange("Other Interchange",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED,Collections.EMPTY_SET),
                new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED,Collections.EMPTY_SET)));

    }

    @Test
    public void testFetchingNeighbourWithCorrectStatus() {


        NeighbourFetcher fetcher = new NeighbourFetcher(repository);

        List<Interchange> interchanges = fetcher.listNeighbourCandidates();
        assertThat(interchanges).size().isEqualTo(1);


    }
}
