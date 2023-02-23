package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class NeighbourRepositorySelectorIT {


	@Autowired
	private NeighbourRepository neighbourRepository;

	private Neighbour createNeighbourObject(String name, NeighbourCapabilities.NeighbourCapabilitiesStatus capStatus, NeighbourSubscriptionRequestStatus subscriptionRequestStatus, SubscriptionRequestStatus fedInStatus){
		Neighbour neighbour = new Neighbour(name,
				new NeighbourCapabilities(capStatus, Collections.emptySet()),
				new NeighbourSubscriptionRequest(subscriptionRequestStatus, new HashSet<>()),
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

		assertThat(interchangeInList("Volvo", volvoInList)).isTrue();
	}

	@Test
	public void helperMethodIsFalseIfInterchangeNotInList(){
		Neighbour tesla = new Neighbour();
		tesla.setName("Tesla");

		List<Neighbour> volvoNotInList = Collections.singletonList(tesla);

		assertThat(interchangeInList("Volvo", volvoNotInList)).isFalse();
	}

	@Test
	public void interchangeWithKnownCapabilitiesAndEmptySubscriptionIsSelectedForSubscriptionRequest(){
		Neighbour ericsson = createNeighbourObject("ericsson-1", NeighbourCapabilities.NeighbourCapabilitiesStatus.KNOWN, NeighbourSubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.EMPTY);

		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangeForSubscriptionRequest = neighbourRepository.findByCapabilities_Status(NeighbourCapabilities.NeighbourCapabilitiesStatus.KNOWN);

		assertThat(interchangeInList(ericsson.getName(), getInterchangeForSubscriptionRequest)).isTrue();
	}

	@Test
	public void interchangeWithUnknownCapabilitiesIsSelectedForCapabilityExchange(){
		Neighbour ericsson = createNeighbourObject("ericsson-2", NeighbourCapabilities.NeighbourCapabilitiesStatus.UNKNOWN, NeighbourSubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.EMPTY);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesForCapabilityExchange = neighbourRepository.findByCapabilities_Status(NeighbourCapabilities.NeighbourCapabilitiesStatus.UNKNOWN);

		assertThat(interchangeInList(ericsson.getName(), getInterchangesForCapabilityExchange)).isTrue();
	}

	@Test
	public void interchangeWithFailedInIsSelectedForGracefulBackoff(){
		Neighbour ericsson = createNeighbourObject("ericsson-3", NeighbourCapabilities.NeighbourCapabilitiesStatus.UNKNOWN, NeighbourSubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.FAILED);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedFedIn = neighbourRepository.findByOurRequestedSubscriptions_StatusIn(SubscriptionRequestStatus.FAILED);

		assertThat(interchangeInList(ericsson.getName(), getInterchangesWithFailedFedIn )).isTrue();
	}

	@Test
	public void interchangeWithFailedCapabilityExchangeIsSelectedForGracefulBackoff(){
		Neighbour ericsson = createNeighbourObject("ericsson-4", NeighbourCapabilities.NeighbourCapabilitiesStatus.FAILED, NeighbourSubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.EMPTY);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedCapabilityExchange = neighbourRepository.findByCapabilities_Status(NeighbourCapabilities.NeighbourCapabilitiesStatus.FAILED);

		assertThat(interchangeInList(ericsson.getName(), getInterchangesWithFailedCapabilityExchange)).isTrue();
	}


	@Test
	public void interchangeWithRequestedSubscriptionsInFedInIsSelectedForPolling(){
		Neighbour ericsson = createNeighbourObject("ericsson-5-R", NeighbourCapabilities.NeighbourCapabilitiesStatus.UNKNOWN, NeighbourSubscriptionRequestStatus.REQUESTED, SubscriptionRequestStatus.REQUESTED);

		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);
		ericsson.getOurRequestedSubscriptions().setSubscriptions(Collections.singleton(subscription));
		neighbourRepository.save(ericsson);

		Subscription subscriptionA = new Subscription();
		subscriptionA.setSelector("originatingCountry = 'OM'");
		subscriptionA.setSubscriptionStatus(SubscriptionStatus.ACCEPTED);
		SubscriptionRequest fedin = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Sets.newSet(subscriptionA));
		NeighbourCapabilities capabilities = new NeighbourCapabilities(NeighbourCapabilities.NeighbourCapabilitiesStatus.UNKNOWN, Collections.emptySet());
		Neighbour ericssonA = new Neighbour("ericsson-5-A", capabilities, null, fedin);
		neighbourRepository.save(ericssonA);

		List<Neighbour> getInterchangeWithRequestedSubscriptionsInFedIn = neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(
				SubscriptionStatus.ACCEPTED, SubscriptionStatus.REQUESTED);

		assertThat(interchangeInList(ericsson.getName(), getInterchangeWithRequestedSubscriptionsInFedIn)).isTrue();
		assertThat(interchangeInList(ericssonA.getName(), getInterchangeWithRequestedSubscriptionsInFedIn)).isTrue();
	}

	@Test
	public void interchangeWithFailedSubscriptionInFedInIsSelectedForBackoff(){
		Neighbour ericsson = createNeighbourObject("ericsson-6", NeighbourCapabilities.NeighbourCapabilitiesStatus.UNKNOWN, NeighbourSubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.FAILED);

		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
		Set<Subscription> subscriptionSet = new HashSet<>();
		subscriptionSet.add(subscription);
		ericsson.getOurRequestedSubscriptions().setSubscriptions(subscriptionSet);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedSubscriptionInFedIn = neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.FAILED);

		assertThat(interchangeInList(ericsson.getName(), getInterchangesWithFailedSubscriptionInFedIn)).isTrue();
	}

	@Test
	public void neighbourWithFedInRequestedIsSelectedForGroups(){
		Neighbour ericsson = createNeighbourObject("ericsson-7", NeighbourCapabilities.NeighbourCapabilitiesStatus.UNKNOWN, NeighbourSubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.REQUESTED);
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		subscriptionRequest.setStatus(SubscriptionRequestStatus.REQUESTED);
		ericsson.setOurRequestedSubscriptions(subscriptionRequest);
		neighbourRepository.save(ericsson);

		List<Neighbour> interchangeWithFedInRequested = neighbourRepository.findByOurRequestedSubscriptions_StatusIn(
				SubscriptionRequestStatus.REQUESTED, SubscriptionRequestStatus.ESTABLISHED);

		assertThat(interchangeInList(ericsson.getName(), interchangeWithFedInRequested)).isTrue();
	}

	@Test
	public void neigbourWithFedInEstablishedIsSelectedForGroups(){
		Neighbour ericsson = createNeighbourObject("ericsson-8", NeighbourCapabilities.NeighbourCapabilitiesStatus.UNKNOWN, NeighbourSubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.ESTABLISHED);
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		subscriptionRequest.setStatus(SubscriptionRequestStatus.ESTABLISHED);
		ericsson.setOurRequestedSubscriptions(subscriptionRequest);
		neighbourRepository.save(ericsson);

		List<Neighbour> interchangeWithFedInEstablished = neighbourRepository.findByOurRequestedSubscriptions_StatusIn(
				SubscriptionRequestStatus.REQUESTED, SubscriptionRequestStatus.ESTABLISHED);

		assertThat(interchangeInList(ericsson.getName(), interchangeWithFedInEstablished)).isTrue();
	}

	@Test
	public void neighbourWithFedInRejectedIsSelectedForRemovalFromGroups(){
		Neighbour ericsson = createNeighbourObject("ericsson-9", NeighbourCapabilities.NeighbourCapabilitiesStatus.UNKNOWN, NeighbourSubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.EMPTY);
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		subscriptionRequest.setStatus(SubscriptionRequestStatus.REJECTED);
		ericsson.setOurRequestedSubscriptions(subscriptionRequest);
		neighbourRepository.save(ericsson);

		List<Neighbour> interchangeWithFedInRejected = neighbourRepository.findByOurRequestedSubscriptions_StatusIn(SubscriptionRequestStatus.REJECTED);

		assertThat(interchangeInList(ericsson.getName(), interchangeWithFedInRejected)).isTrue();
	}

}
