package no.vegvesen.ixn.federation.repository;

/*-
 * #%L
 * neighbour-model
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
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

import static org.assertj.core.api.Assertions.assertThat;

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
		Neighbour ericsson = createNeighbourObject("ericsson-1", Capabilities.CapabilitiesStatus.KNOWN, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.EMPTY);

		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangeForSubscriptionRequest = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.KNOWN);

		assertThat(interchangeInList(ericsson.getName(), getInterchangeForSubscriptionRequest)).isTrue();
	}

	@Test
	public void interchangeWithUnknownCapabilitiesIsSelectedForCapabilityExchange(){
		Neighbour ericsson = createNeighbourObject("ericsson-2", Capabilities.CapabilitiesStatus.UNKNOWN, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.EMPTY);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesForCapabilityExchange = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.UNKNOWN);

		assertThat(interchangeInList(ericsson.getName(), getInterchangesForCapabilityExchange)).isTrue();
	}

	@Test
	public void interchangeWithFailedInIsSelectedForGracefulBackoff(){
		Neighbour ericsson = createNeighbourObject("ericsson-3", Capabilities.CapabilitiesStatus.UNKNOWN, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.FAILED);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedFedIn = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequestStatus.FAILED);

		assertThat(interchangeInList(ericsson.getName(), getInterchangesWithFailedFedIn )).isTrue();
	}

	@Test
	public void interchangeWithFailedCapabilityExchangeIsSelectedForGracefulBackoff(){
		Neighbour ericsson = createNeighbourObject("ericsson-4", Capabilities.CapabilitiesStatus.FAILED, SubscriptionRequestStatus.EMPTY, SubscriptionRequestStatus.EMPTY);
		neighbourRepository.save(ericsson);

		List<Neighbour> getInterchangesWithFailedCapabilityExchange = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED);

		assertThat(interchangeInList(ericsson.getName(), getInterchangesWithFailedCapabilityExchange)).isTrue();
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

		assertThat(interchangeInList(ericsson.getName(), getInterchangeWithRequestedSubscriptionsInFedIn)).isTrue();
		assertThat(interchangeInList(ericssonA.getName(), getInterchangeWithRequestedSubscriptionsInFedIn)).isTrue();
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

		assertThat(interchangeInList(ericsson.getName(), getInterchangesWithFailedSubscriptionInFedIn)).isTrue();
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

		assertThat(interchangeInList(ericsson.getName(), interchangeWithFedInRequested)).isTrue();
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

		assertThat(interchangeInList(ericsson.getName(), interchangeWithFedInEstablished)).isTrue();
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

		assertThat(interchangeInList(ericsson.getName(), interchangeWithFedInRejected)).isTrue();
	}

}
