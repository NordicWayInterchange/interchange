package no.vegvesen.ixn.federation.service;

/*-
 * #%L
 * neighbour-service
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

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.discoverer.NeighbourDiscovererProperties;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourRESTFacade;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotInDNSException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.onboard.SelfService;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NeighbourServiceTest {
	@Mock
	NeighbourRepository neighbourRepository;
	@Mock
	DNSFacade dnsFacade;
	@Mock
	SelfService selfService;
	@Mock
	NeighbourRESTFacade neighbourRESTFacade;


	private NeighbourDiscovererProperties discovererProperties = new NeighbourDiscovererProperties();
	private GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
	private String myName = "bouvet.itsinterchange.eu";

	NeighbourService neighbourService;

	@BeforeEach
	void setUp() {
		neighbourService = new NeighbourService(neighbourRepository, dnsFacade, backoffProperties, discovererProperties, neighbourRESTFacade, myName, selfService);
	}

	@Test
	void isAutowired() {
		assertThat(neighbourService).isNotNull();
	}

	@Test
	void postDatexDataTypeCapability() {
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		DataTypeApi ericssonDataType = new Datex2DataTypeApi("myPublisherId", "myPublisherName", "NO", null, null, Sets.newLinkedHashSet(), "myPublicationType", null);
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Mock dns lookup
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		Mockito.doReturn(Lists.list(ericssonNeighbour)).when(dnsFacade).getNeighbours();
		Mockito.when(selfService.fetchSelf()).thenReturn(new Self("bouvet"));

		CapabilityApi response = neighbourService.incomingCapabilities(ericsson);

		Mockito.verify(dnsFacade, Mockito.times(1)).getNeighbours();
		Mockito.verify(neighbourRepository, Mockito.times(1)).save(ArgumentMatchers.any(Neighbour.class));
		Assertions.assertThat(response.getName()).isEqualTo("bouvet");
	}

	@Test
	void postingSubscriptionRequestFromUnseenNeighbourReturnsException() {
		// Create incoming subscription request api objcet
		SubscriptionRequestApi ericsson = new SubscriptionRequestApi();
		ericsson.setName("ericsson");
		ericsson.setSubscriptions(Collections.singleton(new SubscriptionApi("originatingCountry = 'FI'", "", SubscriptionStatus.REQUESTED)));

		// Mock saving Neighbour to Neighbour repository
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		Subscription firstSubscription = new Subscription("originatingCountry = 'FI'", SubscriptionStatus.REQUESTED);
		firstSubscription.setPath("/ericsson/subscription/1");
		SubscriptionRequest returnedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		Neighbour updatedNeighbour = new Neighbour("ericsson", capabilities, returnedSubscriptionRequest, null);

		// Mock response from DNS facade on Server
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");

		Throwable thrown = Assertions.catchThrowable(() -> neighbourService.incomingSubscriptionRequest(ericsson));

		Assertions.assertThat(thrown).isInstanceOf(SubscriptionRequestException.class);
		Mockito.verify(neighbourRepository, Mockito.times(1)).findByName(ArgumentMatchers.anyString());
		Mockito.verify(dnsFacade, Mockito.times(0)).getNeighbours();
	}

	@Test
	void postingDatexCapabilitiesReturnsStatusCreated() {
		// incoming capabiity API
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		DataTypeApi ericssonDataType = new Datex2DataTypeApi("NO");
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Mock dns lookup
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		Mockito.doReturn(Lists.list(ericssonNeighbour)).when(dnsFacade).getNeighbours();
		when(selfService.fetchSelf()).thenReturn(new Self(myName));

		neighbourService.incomingCapabilities(ericsson);

		Mockito.verify(dnsFacade, Mockito.times(1)).getNeighbours();
	}

	@Test
	public void postingCapabilitiesUnknownInDNSReturnsError() {
		// Mock the incoming API object.
		CapabilityApi unknownNeighbour = new CapabilityApi();
		unknownNeighbour.setName("unknownNeighbour");
		unknownNeighbour.setCapabilities(Collections.singleton(new Datex2DataTypeApi("NO")));

		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		Mockito.doReturn(Lists.list(ericssonNeighbour)).when(dnsFacade).getNeighbours();

		Throwable thrown = Assertions.catchThrowable(() -> neighbourService.incomingCapabilities(unknownNeighbour));

		Assertions.assertThat(thrown).isInstanceOf(InterchangeNotInDNSException.class);
		Mockito.verify(dnsFacade, Mockito.times(1)).getNeighbours();
	}

	@Test
	void postingSubscriptionRequestReturnsStatusAccepted() {

		// Create incoming subscription request api objcet
		SubscriptionRequestApi ericsson = new SubscriptionRequestApi();
		ericsson.setName("ericsson");
		ericsson.setSubscriptions(Collections.singleton(new SubscriptionApi("originatingCountry = 'FI'", "", SubscriptionStatus.REQUESTED)));

		// Mock saving Neighbour to Neighbour repository
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		Subscription firstSubscription = new Subscription("originatingCountry = 'FI'", SubscriptionStatus.REQUESTED);
		firstSubscription.setPath("/ericsson/subscription/1");
		Set<Subscription> subscriptions = Sets.newLinkedHashSet(firstSubscription);
		SubscriptionRequest returnedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour updatedNeighbour = new Neighbour("ericsson", capabilities, returnedSubscriptionRequest, null);

		Mockito.doReturn(updatedNeighbour).when(neighbourRepository).save(ArgumentMatchers.any(Neighbour.class));
		Mockito.doReturn(updatedNeighbour).when(neighbourRepository).findByName(ArgumentMatchers.anyString());

		// Mock response from DNS facade on Server
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");

		neighbourService.incomingSubscriptionRequest(ericsson);
		Mockito.verify(neighbourRepository, Mockito.times(2)).save(ArgumentMatchers.any(Neighbour.class)); //saved twice because first save generates id, and second save saves the path derived from the ids
		Mockito.verify(neighbourRepository, Mockito.times(1)).findByName(ArgumentMatchers.anyString());
		Mockito.verify(dnsFacade, Mockito.times(0)).getNeighbours();
	}

	@Test
	public void findBouvetExists() {
		Mockito.when(dnsFacade.getNeighbours()).thenReturn(Lists.list(new Neighbour("bouveta-fed.itsinterchange.eu", null, null, null)));
		Neighbour neighbour = neighbourService.findNeighbour("bouveta-fed.itsinterchange.eu");
		Assertions.assertThat(neighbour).isNotNull();
	}

	@Test
	public void findNotDefinedDoesNotExists() {
		Mockito.when(dnsFacade.getNeighbours()).thenReturn(Lists.list(new Neighbour("bouveta-fed.itsinterchange.eu", null, null, null)));
		Throwable trown = Assertions.catchThrowable(() -> neighbourService.findNeighbour("no-such-interchange.itsinterchange.eu"));
		Assertions.assertThat(trown).isInstanceOf(InterchangeNotInDNSException.class);
	}

}
