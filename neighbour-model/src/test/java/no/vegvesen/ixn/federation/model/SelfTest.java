package no.vegvesen.ixn.federation.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public class SelfTest {


	private DataType norwayObstruction = new DataType("datex2;1.0", "NO", "Obstruction");
	private DataType swedishWorks = new DataType("datex2;1.0", "SE", "Works");
	private Self self;

	@Before
	public void setUp() {
		self = new Self("Bouvet");
		Set<DataType> selfCapabilities = Collections.singleton(new DataType("datex2;1.0", "NO", "Obstruction"));
		self.setLocalCapabilities(selfCapabilities);
		Set<Subscription> selfSubscriptions = Collections.singleton(new Subscription("where LIKE 'FI'", Subscription.SubscriptionStatus.REQUESTED));
		self.setLocalSubscriptions(selfSubscriptions);

	}

	@Test
	public void subscriptionRequestForNeighbourWithNoCommonInterestsIsEmpty(){

		Neighbour neighbourNeighbour = new Neighbour();
		neighbourNeighbour.setName("BMW");
		neighbourNeighbour.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(swedishWorks)));

		Set<Subscription> calculatedCustomSubscription = self.calculateCustomSubscriptionForNeighbour(neighbourNeighbour);

		Assert.assertEquals(0, calculatedCustomSubscription.size());
	}

	@Test
	public void illegalSelectorGivesEmptySubscription(){
		Neighbour neighbourNeighbour = new Neighbour();
		neighbourNeighbour.setName("BMW");
		neighbourNeighbour.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(swedishWorks)));

		Set<Subscription> calculatedCustomSubscription = self.calculateCustomSubscriptionForNeighbour(neighbourNeighbour);

		Assert.assertTrue(calculatedCustomSubscription.isEmpty());
	}

	@Test
	public void subscriptionRequestForNeighbourWithCommonInterestIsCalculatedCorrectly(){
		Neighbour neighbourNeighbour = new Neighbour();
		neighbourNeighbour.setName("BMW");
		neighbourNeighbour.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,Collections.singleton(norwayObstruction)));
		Subscription overlappingSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		self.setLocalSubscriptions(Collections.singleton(overlappingSubscription));

		Set<Subscription> calculatedCustomSubscription = self.calculateCustomSubscriptionForNeighbour(neighbourNeighbour);

		Assert.assertEquals(1, calculatedCustomSubscription.size());
	}


}
