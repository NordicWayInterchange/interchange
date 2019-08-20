package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NeighbourRepositorySelectorIT {

	private Neighbour ericsson;

	@Autowired
	private NeighbourRepository neighbourRepository;

	@Before
	public void before(){
		ericsson = new Neighbour();
		ericsson.setName("ericsson.itsinterchange.eu");
		ericsson.setControlChannelPort("8080");
		ericsson.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet()));
		ericsson.setSubscriptionRequest(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, new HashSet<>()));
		ericsson.setFedIn(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, new HashSet<>()));
	}

	public boolean interchangeInList(String interchangeName, List<Neighbour> listOfInterchanges){

		for(Neighbour i : listOfInterchanges){
			if(i.getName().equals(interchangeName)){
				return true;
			}
		}
		return false;
	}

	@Test
	public void helperMethodIsTrueIfInterchangeIsInList(){
		Neighbour volvo = new Neighbour();
		volvo.setName("Volvo");

		List<Neighbour> volvoInList = Collections.singletonList(volvo);

		Assert.assertTrue(interchangeInList("Volvo", volvoInList));
	}

	@Test
	public void helperMethodIsFalseIfInterchangeNotInList(){
		Neighbour tesla = new Neighbour();
		tesla.setName("Tesla");

		List<Neighbour> volvoNotInList = Collections.singletonList(tesla);

		Assert.assertFalse(interchangeInList("Volvo", volvoNotInList));
	}

	@Test
	public void interchangeWithKnownCapabilitiesAndEmptySubscriptionIsSelectedForSubscriptionRequest(){
		ericsson.setName("ericsson-1");
		ericsson.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		ericsson.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangeForSubscriptionRequest = neighbourRepository.findNeighboursByCapabilities_Status_AndFedIn_Status(Capabilities.CapabilitiesStatus.KNOWN, SubscriptionRequest.SubscriptionRequestStatus.EMPTY);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangeForSubscriptionRequest));
	}

	@Test
	public void interchangeWithUnknownCapabilitiesIsSelectedForCapabilityExchange(){
		ericsson.setName("ericsson-2");
		ericsson.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesForCapabilityExchange = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.UNKNOWN);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesForCapabilityExchange));
	}

	@Test
	public void interchangeWithFailedInIsSelectedForGracefulBackoff(){
		ericsson.setName("ericsson-3");
		ericsson.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.FAILED);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedFedIn = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.FAILED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesWithFailedFedIn ));
	}

	@Test
	public void interchangeWithFailedCapabilityExchangeIsSelectedForGracefulBackoff(){
		ericsson.setName("ericsson-4");
		ericsson.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.FAILED);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedCapabilityExchange = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesWithFailedCapabilityExchange));
	}


	@Test
	public void interchangeWithRequestedSubscriptionsInFedInIsSelectedForPolling(){
		ericsson.setName("ericsson-5-R");

		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
		ericsson.getFedIn().setSubscriptions(Collections.singleton(subscription));
		ericsson.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		neighbourRepository.save(ericsson);

		Subscription subscriptionA = new Subscription();
		subscriptionA.setSelector("where LIKE 'OM'");
		subscriptionA.setSubscriptionStatus(Subscription.SubscriptionStatus.ACCEPTED);
		SubscriptionRequest fedin = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Sets.newSet(subscriptionA));
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		Neighbour ericssonA = new Neighbour("ericsson-5-A", capabilities, null, fedin);
		neighbourRepository.save(ericssonA);

		List<Neighbour> getInterchangeWithRequestedSubscriptionsInFedIn = neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(
				Subscription.SubscriptionStatus.ACCEPTED, Subscription.SubscriptionStatus.REQUESTED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangeWithRequestedSubscriptionsInFedIn));
		Assert.assertTrue(interchangeInList(ericssonA.getName(), getInterchangeWithRequestedSubscriptionsInFedIn));
	}

	@Test
	public void interchangeWithFailedSubscriptionInFedInIsSelectedForBackoff(){
		ericsson.setName("ericsson-6");

		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.FAILED);
		Set<Subscription> subscriptionSet = new HashSet<>();
		subscriptionSet.add(subscription);
		ericsson.getFedIn().setSubscriptions(subscriptionSet);
		ericsson.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedSubscriptionInFedIn = neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(Subscription.SubscriptionStatus.FAILED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesWithFailedSubscriptionInFedIn));
	}

	@Test
	public void neighbourWithFedInRequestedIsSelectedForGroups(){

		ericsson.setName("ericsson-7");
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		subscriptionRequest.setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		ericsson.setFedIn(subscriptionRequest);
		neighbourRepository.save(ericsson);

		List<Neighbour> interchangeWithFedInRequested = neighbourRepository.findByFedIn_StatusIn(
				SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), interchangeWithFedInRequested));
	}

	@Test
	public void neigbourWithFedInEstablishedIsSelectedForGroups(){
		ericsson.setName("ericsson-8");
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		subscriptionRequest.setStatus(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
		ericsson.setFedIn(subscriptionRequest);
		neighbourRepository.save(ericsson);

		List<Neighbour> interchangeWithFedInEstablished = neighbourRepository.findByFedIn_StatusIn(
				SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), interchangeWithFedInEstablished));
	}

	@Test
	public void neighbourWithFedInRejectedIsSelectedForRemovalFromGroups(){
		ericsson.setName("ericsson-9");
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		subscriptionRequest.setStatus(SubscriptionRequest.SubscriptionRequestStatus.REJECTED);
		ericsson.setFedIn(subscriptionRequest);
		neighbourRepository.save(ericsson);

		List<Neighbour> interchangeWithFedInRejected = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.REJECTED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), interchangeWithFedInRejected));
	}

}
