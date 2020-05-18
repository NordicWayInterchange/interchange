package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.discoverer.NeighbourDiscovererProperties;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourRESTFacade;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.federation.service.NeighbourService;
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
import static org.mockito.Mockito.mock;

//TODO this might have to use a Docker psql database.
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class NeighbourServiceIT {

    @Autowired
    private NeighbourRepository repository;

    @Before
    public void setUp() {
        Neighbour interchangeA = new Neighbour("interchangeA",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.emptySet()),
                new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.emptySet()));
        Neighbour interchangeB = new Neighbour("interchangeB",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.emptySet()),
                new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.emptySet()));
        repository.save(interchangeA);
        repository.save(interchangeB);
    }

    @Test
    public void testFetchingNeighbourWithCorrectStatus() {
        NeighbourService fetcher = new NeighbourService(repository, mock(SelfRepository.class), mock(DNSFacade.class), new GracefulBackoffProperties(), new NeighbourDiscovererProperties(), mock(NeighbourRESTFacade.class), "myName");
        //List<Neighbour> interchanges = fetcher.listNeighbourCandidates();
        List<Neighbour> interchanges = fetcher.listNeighboursToConsumeMessagesFrom();
        assertThat(interchanges).size().isEqualTo(1);
    }
}
