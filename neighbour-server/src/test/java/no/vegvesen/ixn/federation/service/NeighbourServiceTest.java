package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotInDNSException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


class NeighbourServiceTest {
	private NeighbourRepository neighbourRepository = mock(NeighbourRepository.class);
	private DNSFacade dnsFacade = mock(DNSFacade.class);
	private SelfRepository selfRepository = mock(SelfRepository.class);

	NeighbourService neighbourService;

	@BeforeEach
	void setUp() {
		neighbourService = new NeighbourService(neighbourRepository, selfRepository, dnsFacade);
		when(selfRepository.findByName(any())).thenReturn(new Self("bouvet"));
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
		doReturn(ericssonNeighbour).when(dnsFacade).findNeighbour(anyString());

		CapabilityApi response = neighbourService.incomingCapabilities(ericsson);

		verify(dnsFacade, times(1)).findNeighbour(anyString());
		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
		assertThat(response.getName()).isEqualTo("bouvet");
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

		doReturn(updatedNeighbour).when(neighbourRepository).save(any(Neighbour.class));

		// Mock response from DNS facade on Server
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		doReturn(Collections.singletonList(ericssonNeighbour)).when(dnsFacade).getNeighbours();

		Throwable thrown = catchThrowable(() -> neighbourService.incomingSubscriptionRequest(ericsson));

		assertThat(thrown).isInstanceOf(SubscriptionRequestException.class);
		verify(neighbourRepository, times(1)).findByName(anyString());
		verify(dnsFacade, times(0)).getNeighbours();
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
		doReturn(ericssonNeighbour).when(dnsFacade).findNeighbour(anyString());

		neighbourService.incomingCapabilities(ericsson);

		verify(dnsFacade, times(1)).findNeighbour(anyString());
	}

	@Test
	public void postingCapabilitiesUnknownInDNSReturnsError() {
		// Mock the incoming API object.
		CapabilityApi unknownNeighbour = new CapabilityApi();
		unknownNeighbour.setName("unknownNeighbour");
		unknownNeighbour.setCapabilities(Collections.singleton(new Datex2DataTypeApi("NO")));

		when(dnsFacade.findNeighbour(any())).thenThrow(InterchangeNotInDNSException.class);

		Throwable thrown = catchThrowable(() -> neighbourService.incomingCapabilities(unknownNeighbour));

		assertThat(thrown).isInstanceOf(InterchangeNotInDNSException.class);
		verify(dnsFacade, times(1)).findNeighbour("unknownNeighbour");
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

		doReturn(updatedNeighbour).when(neighbourRepository).save(any(Neighbour.class));
		doReturn(updatedNeighbour).when(neighbourRepository).findByName(anyString());

		// Mock response from DNS facade on Server
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		doReturn(Collections.singletonList(ericssonNeighbour)).when(dnsFacade).getNeighbours();

		neighbourService.incomingSubscriptionRequest(ericsson);
		verify(neighbourRepository, times(2)).save(any(Neighbour.class)); //saved twice because first save generates id, and second save saves the path derived from the ids
		verify(neighbourRepository, times(1)).findByName(anyString());
		verify(dnsFacade, times(0)).getNeighbours();
	}

}