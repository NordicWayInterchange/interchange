package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class NeighbourRepositorySelectorIT {


	@Autowired
	private NeighbourRepository neighbourRepository;

	private Neighbour createNeighbourObject(String name, Capabilities.CapabilitiesStatus capStatus, SubscriptionRequestStatus subscriptionRequestStatus, SubscriptionRequestStatus fedInStatus){
		Neighbour neighbour = new Neighbour(name,
				new Capabilities(capStatus, Collections.emptySet()),
				new SubscriptionRequest(subscriptionRequestStatus, new HashSet<>()),
				new SubscriptionRequest(fedInStatus, new HashSet<>()));
		neighbour.setControlChannelPort("8080");
		return neighbour;
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
		Neighbour ericsson = createNeighbourObject("ericsson-1", Capabilities.CapabilitiesStatus.KNOWN, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.EMPTY);

		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangeForSubscriptionRequest = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.KNOWN);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangeForSubscriptionRequest));
	}

	@Test
	public void interchangeWithUnknownCapabilitiesIsSelectedForCapabilityExchange(){
		Neighbour ericsson = createNeighbourObject("ericsson-2", Capabilities.CapabilitiesStatus.UNKNOWN, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.EMPTY);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesForCapabilityExchange = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.UNKNOWN);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesForCapabilityExchange));
	}

	@Test
	public void interchangeWithFailedInIsSelectedForGracefulBackoff(){
		Neighbour ericsson = createNeighbourObject("ericsson-3", Capabilities.CapabilitiesStatus.UNKNOWN, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.FAILED);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedFedIn = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequestStatus.FAILED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesWithFailedFedIn ));
	}

	@Test
	public void interchangeWithFailedCapabilityExchangeIsSelectedForGracefulBackoff(){
		Neighbour ericsson = createNeighbourObject("ericsson-4", Capabilities.CapabilitiesStatus.FAILED, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.EMPTY);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedCapabilityExchange = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesWithFailedCapabilityExchange));
	}


	@Test
	public void interchangeWithRequestedSubscriptionsInFedInIsSelectedForPolling(){
		Neighbour ericsson = createNeighbourObject("ericsson-5-R", Capabilities.CapabilitiesStatus.UNKNOWN, SubscriptionRequestStatus.REQUESTED, SubscriptionRequestStatus.REQUESTED);

		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);
		ericsson.getFedIn().setSubscriptions(Collections.singleton(subscription));
		neighbourRepository.save(ericsson);

		Subscription subscriptionA = new Subscription();
		subscriptionA.setSelector("originatingCountry = 'OM'");
		subscriptionA.setSubscriptionStatus(SubscriptionStatus.ACCEPTED);
		SubscriptionRequest fedin = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Sets.newSet(subscriptionA));
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		Neighbour ericssonA = new Neighbour("ericsson-5-A", capabilities, null, fedin);
		neighbourRepository.save(ericssonA);

		List<Neighbour> getInterchangeWithRequestedSubscriptionsInFedIn = neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(
				SubscriptionStatus.ACCEPTED, SubscriptionStatus.REQUESTED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangeWithRequestedSubscriptionsInFedIn));
		Assert.assertTrue(interchangeInList(ericssonA.getName(), getInterchangeWithRequestedSubscriptionsInFedIn));
	}

	@Test
	public void interchangeWithFailedSubscriptionInFedInIsSelectedForBackoff(){
		Neighbour ericsson = createNeighbourObject("ericsson-6", Capabilities.CapabilitiesStatus.UNKNOWN, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.FAILED);

		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
		Set<Subscription> subscriptionSet = new HashSet<>();
		subscriptionSet.add(subscription);
		ericsson.getFedIn().setSubscriptions(subscriptionSet);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedSubscriptionInFedIn = neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus.FAILED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesWithFailedSubscriptionInFedIn));
	}

	@Test
	public void neighbourWithFedInRequestedIsSelectedForGroups(){
		Neighbour ericsson = createNeighbourObject("ericsson-7", Capabilities.CapabilitiesStatus.UNKNOWN, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.REQUESTED);
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		subscriptionRequest.setStatus(SubscriptionRequestStatus.REQUESTED);
		ericsson.setFedIn(subscriptionRequest);
		neighbourRepository.save(ericsson);

		List<Neighbour> interchangeWithFedInRequested = neighbourRepository.findByFedIn_StatusIn(
				SubscriptionRequestStatus.REQUESTED, SubscriptionRequestStatus.ESTABLISHED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), interchangeWithFedInRequested));
	}

	@Test
	public void neigbourWithFedInEstablishedIsSelectedForGroups(){
		Neighbour ericsson = createNeighbourObject("ericsson-8", Capabilities.CapabilitiesStatus.UNKNOWN, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.ESTABLISHED);
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		subscriptionRequest.setStatus(SubscriptionRequestStatus.ESTABLISHED);
		ericsson.setFedIn(subscriptionRequest);
		neighbourRepository.save(ericsson);

		List<Neighbour> interchangeWithFedInEstablished = neighbourRepository.findByFedIn_StatusIn(
				SubscriptionRequestStatus.REQUESTED, SubscriptionRequestStatus.ESTABLISHED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), interchangeWithFedInEstablished));
	}

	@Test
	public void neighbourWithFedInRejectedIsSelectedForRemovalFromGroups(){
		Neighbour ericsson = createNeighbourObject("ericsson-9", Capabilities.CapabilitiesStatus.UNKNOWN, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.EMPTY);
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		subscriptionRequest.setStatus(SubscriptionRequestStatus.REJECTED);
		ericsson.setFedIn(subscriptionRequest);
		neighbourRepository.save(ericsson);

		List<Neighbour> interchangeWithFedInRejected = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequestStatus.REJECTED);

		Assert.assertTrue(interchangeInList(ericsson.getName(), interchangeWithFedInRejected));
	}

}
