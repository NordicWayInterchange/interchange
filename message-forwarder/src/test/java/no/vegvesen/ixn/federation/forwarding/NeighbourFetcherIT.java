package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.junit.Before;
import org.junit.Ignore;
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
@Ignore
public class NeighbourFetcherIT {

    @Autowired
    private NeighbourRepository repository;

    @Before
    public void setUp() {
        repository.save(new Neighbour("interchangeA",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED,Collections.EMPTY_SET),
                new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED,Collections.EMPTY_SET)));
        repository.save(new Neighbour("interchangeB",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED,Collections.EMPTY_SET),
                new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED,Collections.EMPTY_SET)));

    }

    @Test
    public void testFetchingNeighbourWithCorrectStatus() {
        NeighbourFetcher fetcher = new NeighbourFetcher(repository);
        List<Neighbour> interchanges = fetcher.listNeighbourCandidates();
        assertThat(interchanges).size().isEqualTo(1);


    }
}
