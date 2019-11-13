package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.DiscoveryStateRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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


	@Test
	public void calculatedSubscriptionRequestSameAsNeighbourSubscriptionsAllowsNextNeighbourToBeSaved() {
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
		Set<Subscription> selfLocalSubscriptions = new HashSet<>();
		selfLocalSubscriptions.add(new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.CREATED));
		self.setLocalSubscriptions(selfLocalSubscriptions);

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED, Collections.emptySet());
		DataType neighbourDataType = new DataType("datex","NO","Conditions");
		Set<DataType> dataTypeSet = new HashSet<>();
		dataTypeSet.add(neighbourDataType);
		Capabilities neighbourCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,dataTypeSet);
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilities,subscriptionRequest,new SubscriptionRequest());
		Set<Subscription> neighbourFedInSubscription = new HashSet<>();
		neighbourFedInSubscription.add(new Subscription("where LIKE 'NO'",Subscription.SubscriptionStatus.ACCEPTED));
		neighbour.setFedIn(new SubscriptionRequest(null,neighbourFedInSubscription));

		Assert.assertTrue(neighbour.hasEstablishedSubscriptions());
		Set<Subscription> subscriptions = self.calculateCustomSubscriptionForNeighbour(neighbour);
		Assert.assertFalse(subscriptions.isEmpty());
		Assert.assertEquals(subscriptions,neighbour.getFedIn().getSubscriptions());

		Neighbour otherNeighbour = new Neighbour("otherNeighbour",
				new Capabilities(),
				new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED,Collections.emptySet()),
				new SubscriptionRequest());

		neighbourDiscoverer.subscriptionRequest(Arrays.asList(neighbour,otherNeighbour),self);
		verify(neighbourRepository).save(otherNeighbour);

	}
}
