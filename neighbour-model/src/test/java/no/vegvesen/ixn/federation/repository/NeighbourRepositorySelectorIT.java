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


	private Neighbour createNeighbourObject(String name){
		Neighbour neighbour = new Neighbour(name,
				new Capabilities(Collections.emptySet()),
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
	public void interchangeWithRequestedSubscriptionsInFedInIsSelectedForPolling(){
		Neighbour ericsson = createNeighbourObject("ericsson-5-R");

		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);
		ericsson.getOurRequestedSubscriptions().setSubscriptions(Collections.singleton(subscription));
		neighbourRepository.save(ericsson);

		Subscription subscriptionA = new Subscription();
		subscriptionA.setSelector("originatingCountry = 'OM'");
		subscriptionA.setSubscriptionStatus(SubscriptionStatus.ACCEPTED);
		SubscriptionRequest fedin = new SubscriptionRequest(Sets.newSet(subscriptionA));
		Capabilities capabilities = new Capabilities(Collections.emptySet());
		Neighbour ericssonA = new Neighbour("ericsson-5-A", capabilities, null, fedin);
		neighbourRepository.save(ericssonA);

		List<Neighbour> getInterchangeWithRequestedSubscriptionsInFedIn = neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(
				SubscriptionStatus.ACCEPTED, SubscriptionStatus.REQUESTED);

		assertThat(interchangeInList(ericsson.getName(), getInterchangeWithRequestedSubscriptionsInFedIn)).isTrue();
		assertThat(interchangeInList(ericssonA.getName(), getInterchangeWithRequestedSubscriptionsInFedIn)).isTrue();
	}

	@Test
	public void interchangeWithFailedSubscriptionInFedInIsSelectedForBackoff() {
		Neighbour ericsson = createNeighbourObject("ericsson-6");

		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
		Set<Subscription> subscriptionSet = new HashSet<>();
		subscriptionSet.add(subscription);
		ericsson.getOurRequestedSubscriptions().setSubscriptions(subscriptionSet);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedSubscriptionInFedIn = neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.FAILED);

		assertThat(interchangeInList(ericsson.getName(), getInterchangesWithFailedSubscriptionInFedIn)).isTrue();
	}
}
