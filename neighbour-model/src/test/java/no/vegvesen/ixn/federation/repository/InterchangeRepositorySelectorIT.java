package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InterchangeRepositorySelectorIT {

	private Interchange ericsson;

	@Autowired
	private InterchangeRepository interchangeRepository;

	@Before
	public void before(){
		ericsson = new Interchange();
		ericsson.setName("ericsson");
		ericsson.setDomainName("itsinterchange.eu");
		ericsson.setControlChannelPort("8080");
		ericsson.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet()));
		ericsson.setSubscriptionRequest(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, new HashSet<>()));
		ericsson.setFedIn(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, new HashSet<>()));
	}

	public boolean interchangeInList(String interchangeName, List<Interchange> listOfInterchanges){

		for(Interchange i : listOfInterchanges){
			if(i.getName().equals(interchangeName)){
				return true;
			}
		}
		return false;
	}

	@Test
	public void helperMethodIsTrueIfInterchangeIsInList(){
		Interchange volvo = new Interchange();
		volvo.setName("Volvo");

		List<Interchange> volvoInList = Collections.singletonList(volvo);

		Assert.assertTrue(interchangeInList("Volvo", volvoInList));
	}

	@Test
	public void helperMethodIsFalseIfInterchangeNotInList(){
		Interchange tesla = new Interchange();
		tesla.setName("Tesla");

		List<Interchange> volvoNotInList = Collections.singletonList(tesla);

		Assert.assertFalse(interchangeInList("Volvo", volvoNotInList));
	}

	@Test
	public void interchangeWithKnownCapabilitiesAndEmptySubscriptionIsSelectedForSubscriptionRequest(){
		ericsson.setName("ericsson-1");
		ericsson.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		ericsson.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
		interchangeRepository.save(ericsson);

		List<Interchange> getInterchangeForSubscriptionRequest = interchangeRepository.findInterchangesForSubscriptionRequest();

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangeForSubscriptionRequest));
	}

	@Test
	public void interchangeWithUnknownCapabilitiesIsSelectedForCapabilityExchange(){
		ericsson.setName("ericsson-2");
		ericsson.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
		interchangeRepository.save(ericsson);

		List<Interchange> getInterchangesForCapabilityExchange = interchangeRepository.findInterchangesForCapabilityExchange();

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesForCapabilityExchange));
	}

	@Test
	public void interchangeWithFailedInIsSelectedForGracefulBackoff(){
		ericsson.setName("ericsson-3");
		ericsson.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.FAILED);
		interchangeRepository.save(ericsson);

		List<Interchange> getInterchangesWithFailedFedIn = interchangeRepository.findInterchangesWithFailedFedIn();

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesWithFailedFedIn ));
	}

	@Test
	public void interchangeWithFailedCapabilityExchangeIsSelectedForGracefulBackoff(){
		ericsson.setName("ericsson-4");
		ericsson.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.FAILED);
		interchangeRepository.save(ericsson);

		List<Interchange> getInterchangesWithFailedCapabilityExchange = interchangeRepository.findInterchangesWithFailedCapabilityExchange();

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesWithFailedCapabilityExchange));
	}


	@Test
	public void interchangeWithRequestedSubscriptionsInFedInIsSelectedForPolling(){
		ericsson.setName("ericsson-5");

		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
		ericsson.getFedIn().setSubscriptions(Collections.singleton(subscription));
		ericsson.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		interchangeRepository.save(ericsson);

		List<Interchange> getInterchangeWithRequestedSubscriptionsInFedIn = interchangeRepository.findInterchangesWithSubscriptionToPoll();

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangeWithRequestedSubscriptionsInFedIn));
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
		interchangeRepository.save(ericsson);

		List<Interchange> getInterchangesWithFailedSubscriptionInFedIn = interchangeRepository.findInterchangesWithFailedSubscriptionsInFedIn();

		Assert.assertTrue(interchangeInList(ericsson.getName(), getInterchangesWithFailedSubscriptionInFedIn));
	}

}
