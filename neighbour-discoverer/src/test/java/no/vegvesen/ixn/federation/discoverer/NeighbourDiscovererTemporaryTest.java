package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Self;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.repository.DiscoveryStateRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NeighbourDiscovererTemporaryTest {
	@Test
	public void twoNeighboursWhereOneHasNoOverlapBeforeAndAfterNewSubscriptionCalculationLetsSecondOneBeCalculated() {
		DNSFacade dnsFacade = mock(DNSFacade.class);
		NeighbourRepository neighbourRepository = mock(NeighbourRepository.class);
		SelfRepository selfRepository = mock(SelfRepository.class);
		DiscoveryStateRepository discoveryStateRepository = mock(DiscoveryStateRepository.class);
		NeighbourRESTFacade neighbourRESTFacade = mock(NeighbourRESTFacade.class);

	    NeighbourDiscoverer neighbourDiscoverer = new NeighbourDiscoverer(dnsFacade,
				neighbourRepository,
				selfRepository,
				discoveryStateRepository,
				neighbourRESTFacade,
				"bouvet",
				new GracefulBackoffProperties(),
				new NeighbourDiscovererProperties());
		Self self = new Self("self");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED, Collections.emptySet());
		Neighbour neighbour = new Neighbour("neighbour",new Capabilities(),subscriptionRequest,new SubscriptionRequest());

		//just to test that I have set up the neighbour and self correctly for the actual test.
		Assert.assertTrue(neighbour.hasEstablishedSubscriptions());
		Assert.assertTrue(self.calculateCustomSubscriptionForNeighbour(neighbour).isEmpty());
		Assert.assertTrue(neighbour.getFedIn().getSubscriptions().isEmpty());

		Neighbour otherNeighbour = new Neighbour("otherNeighbour",
				new Capabilities(),
				new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED,Collections.emptySet()),
				new SubscriptionRequest());


	    neighbourDiscoverer.subscriptionRequest(Arrays.asList(neighbour,otherNeighbour),self);
	    verify(neighbourRepository,times(2)).save(any(Neighbour.class));

	}
}
