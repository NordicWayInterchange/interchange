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

	private Neighbour createNeighbourObject(String name, CapabilitiesStatus capStatus){
		Neighbour neighbour = new Neighbour(name,
				new NeighbourCapabilities(capStatus, Collections.emptySet()),
				new NeighbourSubscriptionRequest(new HashSet<>()),
				new SubscriptionRequest(new HashSet<>()));
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
		Neighbour ericsson = createNeighbourObject("ericsson-1", CapabilitiesStatus.KNOWN);

		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangeForSubscriptionRequest = neighbourRepository.findByCapabilities_StatusAndIgnoreIs(CapabilitiesStatus.KNOWN, false);

		assertThat(interchangeInList(ericsson.getName(), getInterchangeForSubscriptionRequest)).isTrue();
	}

	@Test
	public void interchangeWithUnknownCapabilitiesIsSelectedForCapabilityExchange(){
		Neighbour ericsson = createNeighbourObject("ericsson-2", CapabilitiesStatus.UNKNOWN);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesForCapabilityExchange = neighbourRepository.findByCapabilities_StatusAndIgnoreIs(CapabilitiesStatus.UNKNOWN, false);

		assertThat(interchangeInList(ericsson.getName(), getInterchangesForCapabilityExchange)).isTrue();
	}

	@Test
	public void interchangeWithFailedCapabilityExchangeIsSelectedForGracefulBackoff(){
		Neighbour ericsson = createNeighbourObject("ericsson-4", CapabilitiesStatus.FAILED);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedCapabilityExchange = neighbourRepository.findByCapabilities_StatusAndIgnoreIs(CapabilitiesStatus.FAILED, false);

		assertThat(interchangeInList(ericsson.getName(), getInterchangesWithFailedCapabilityExchange)).isTrue();
	}


	@Test
	public void interchangeWithRequestedSubscriptionsInFedInIsSelectedForPolling(){
		Neighbour ericsson = createNeighbourObject("ericsson-5-R", CapabilitiesStatus.UNKNOWN);

		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);
		ericsson.getOurRequestedSubscriptions().setSubscriptions(Collections.singleton(subscription));
		neighbourRepository.save(ericsson);

		Subscription subscriptionA = new Subscription();
		subscriptionA.setSelector("originatingCountry = 'OM'");
		subscriptionA.setSubscriptionStatus(SubscriptionStatus.REQUESTED);
		SubscriptionRequest fedin = new SubscriptionRequest(Sets.newSet(subscriptionA));
		NeighbourCapabilities capabilities = new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		Neighbour ericssonA = new Neighbour("ericsson-5-A", capabilities, null, fedin);
		neighbourRepository.save(ericssonA);

		List<Neighbour> getInterchangeWithRequestedSubscriptionsInFedIn = neighbourRepository.findDistinctNeighboursByIgnoreIsAndOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(false,
				 SubscriptionStatus.REQUESTED);

		assertThat(interchangeInList(ericsson.getName(), getInterchangeWithRequestedSubscriptionsInFedIn)).isTrue();
		assertThat(interchangeInList(ericssonA.getName(), getInterchangeWithRequestedSubscriptionsInFedIn)).isTrue();
	}

	@Test
	public void interchangeWithFailedSubscriptionInFedInIsSelectedForBackoff() {
		Neighbour ericsson = createNeighbourObject("ericsson-6", CapabilitiesStatus.UNKNOWN);

		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
		Set<Subscription> subscriptionSet = new HashSet<>();
		subscriptionSet.add(subscription);
		ericsson.getOurRequestedSubscriptions().setSubscriptions(subscriptionSet);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedSubscriptionInFedIn = neighbourRepository.findDistinctNeighboursByIgnoreIsAndOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(false, SubscriptionStatus.FAILED);

		assertThat(interchangeInList(ericsson.getName(), getInterchangesWithFailedSubscriptionInFedIn)).isTrue();
	}
}
