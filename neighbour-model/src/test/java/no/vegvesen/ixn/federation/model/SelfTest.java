package no.vegvesen.ixn.federation.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public class SelfTest {


	private DataType norwayObstruction = new DataType("datex2;1.0", "NO", "Obstruction");
	private DataType norwayWorks = new DataType("datex2;1.0","NO","Works");
	private DataType norwayObstruction20 =  new DataType("datex;2.0","NO","Obstruction");
	private DataType swedishWorks = new DataType("datex2;1.0", "SE", "Works");
	private DataType swedishDenm = new DataType("denm","SE","Stuff");
	private Self self;
	private String selfSelector = "where LIKE 'FI'";

	@Before
	public void setUp() {
		self = new Self("Bouvet");
		Set<DataType> selfCapabilities = Collections.singleton(new DataType("datex2;1.0", "NO", "Obstruction"));
		self.setLocalCapabilities(selfCapabilities);
		Set<Subscription> selfSubscriptions = Collections.singleton(new Subscription(selfSelector, Subscription.SubscriptionStatus.REQUESTED));
		self.setLocalSubscriptions(selfSubscriptions);

	}

	@Test
	public void subscriptionRequestForNeighbourWithNoCommonInterestsIsEmpty(){

		Neighbour neighbour = new Neighbour();
		neighbour.setName("BMW");
		neighbour.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(swedishWorks)));

		Set<Subscription> calculatedCustomSubscription = self.calculateCustomSubscriptionForNeighbour(neighbour);

		Assert.assertEquals(0, calculatedCustomSubscription.size());



		//Set<DataType> dataTypes = neighbourNeighbour.getCapabilities().getDataTypes();
		Set<DataType> dataTypes = Collections.singleton(swedishWorks);
		//Set<String> selectors = self.getLocalSubscriptions().stream().map(Subscription::getSelector).collect(Collectors.toSet());
		Set<String> selectors = Collections.singleton(selfSelector);
		Set<Subscription> otherCalculatedSubscriptions = Self.calculateCommonInterestSubscriptions(dataTypes, selectors);
		Assert.assertEquals(0, otherCalculatedSubscriptions.size());
		Assert.assertEquals(calculatedCustomSubscription,otherCalculatedSubscriptions);

	}

	@Test
	public void illegalSelectorGivesEmptySubscription(){
		Neighbour neighbour = new Neighbour();
		neighbour.setName("BMW");
		neighbour.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(swedishWorks)));

		Set<Subscription> calculatedCustomSubscription = self.calculateCustomSubscriptionForNeighbour(neighbour);

		Assert.assertTrue(calculatedCustomSubscription.isEmpty());

		//Set<DataType> dataTypes = neighbourNeighbour.getCapabilities().getDataTypes();
		Set<DataType> dataTypes = Collections.singleton(swedishWorks);
		//Set<String> selectors = self.getLocalSubscriptions().stream().map(Subscription::getSelector).collect(Collectors.toSet());
		Set<String> selectors = Collections.singleton(selfSelector);
		Set<Subscription> otherCalculatedSubscriptions = Self.calculateCommonInterestSubscriptions(dataTypes, selectors);
		Assert.assertTrue(otherCalculatedSubscriptions.isEmpty());
		Assert.assertEquals(calculatedCustomSubscription,otherCalculatedSubscriptions);
	}

	@Test
	public void subscriptionRequestForNeighbourWithCommonInterestIsCalculatedCorrectly(){
		Neighbour neighbour = new Neighbour();
		neighbour.setName("BMW");
		neighbour.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,Collections.singleton(norwayObstruction)));
		String selector = "where LIKE 'NO'";
		Subscription overlappingSubscription = new Subscription(selector, Subscription.SubscriptionStatus.REQUESTED);
		self.setLocalSubscriptions(Collections.singleton(overlappingSubscription));

		Set<Subscription> calculatedCustomSubscription = self.calculateCustomSubscriptionForNeighbour(neighbour);

		Assert.assertEquals(1, calculatedCustomSubscription.size());

		//Set<DataType> dataTypes = neighbourNeighbour.getCapabilities().getDataTypes();
		Set<DataType> dataTypes = Collections.singleton(norwayObstruction);
		//Set<String> selectors = self.getLocalSubscriptions().stream().map(Subscription::getSelector).collect(Collectors.toSet());
		Set<String> selectors = Collections.singleton(selector);
		Set<Subscription> otherCalculatedSubscriptions = Self.calculateCommonInterestSubscriptions(dataTypes, selectors);
		Assert.assertEquals(1,otherCalculatedSubscriptions.size());
		Assert.assertEquals(calculatedCustomSubscription,otherCalculatedSubscriptions);
	}


}
