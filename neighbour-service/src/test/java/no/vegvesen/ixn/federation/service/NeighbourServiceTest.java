package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.RequestedSubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesApi;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotInDNSException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.shared.capability.CapabilityApi;
import no.vegvesen.ixn.shared.capability.DatexApplicationApi;
import no.vegvesen.ixn.shared.capability.MetadataApi;
import no.vegvesen.ixn.shared.capability.RedirectStatusApi;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NeighbourServiceTest {
	@Mock
	NeighbourRepository neighbourRepository;
	@Mock
	DNSFacade dnsFacade;

	private final String myName = "bouvet.itsinterchange.eu";

	NeighbourService neighbourService;

	@BeforeEach
	void setUp() {
		InterchangeNodeProperties interchangeNodeProperties = new InterchangeNodeProperties(myName, "5671");
		neighbourService = new NeighbourService(neighbourRepository, dnsFacade,interchangeNodeProperties);
	}

	@Test
	void isAutowired() {
		assertThat(neighbourService).isNotNull();
	}


	@Test
	void postDatexDataTypeCapability() {
		CapabilitiesApi ericsson = new CapabilitiesApi();
		ericsson.setName("ericsson");
		CapabilityApi ericssonDataType = new CapabilityApi(new DatexApplicationApi("myPublisherId", "pub-1", "NO", null, List.of(), "myPublicationType", "publisherName"), new MetadataApi());
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Mock dns lookup
		Neighbour ericssonNeighbour = new Neighbour("ericsson", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest());
		doReturn(Lists.list(ericssonNeighbour)).when(dnsFacade).lookupNeighbours();

        CapabilitiesApi response = neighbourService.incomingCapabilities(ericsson, Collections.emptySet());

		verify(dnsFacade, times(1)).lookupNeighbours();
		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
		assertThat(response.getName()).isEqualTo(myName);
	}

	@Test
	void postingSubscriptionRequestFromUnseenNeighbourReturnsException() {
		// Create incoming subscription request api objcet
		SubscriptionRequestApi ericsson = new SubscriptionRequestApi("ericsson",Collections.singleton(
				new RequestedSubscriptionApi("originatingCountry = 'FI'")
		));

		// Mock saving Neighbour to Neighbour repository
		Subscription firstSubscription = new Subscription("originatingCountry = 'FI'", SubscriptionStatus.REQUESTED, "ericsson");
		firstSubscription.setPath("/ericsson/subscriptions/1");

		when(neighbourRepository.findByName(anyString())).thenReturn(null);
		Throwable thrown = catchThrowable(() -> neighbourService.incomingSubscriptionRequest(ericsson));

		assertThat(thrown).isInstanceOf(SubscriptionRequestException.class);
		verify(neighbourRepository, times(1)).findByName(anyString());
		verify(dnsFacade, times(0)).lookupNeighbours();
	}

	@Test
	void postingDatexCapabilitiesReturnsStatusCreated() {
		// incoming capabiity API
		CapabilitiesApi ericsson = new CapabilitiesApi();
		ericsson.setName("ericsson");
		CapabilityApi ericssonDataType = new CapabilityApi(new DatexApplicationApi("", "", "NO", "", List.of(), "", "publisherName"), new MetadataApi());
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Mock dns lookup
		Neighbour ericssonNeighbour = new Neighbour("ericsson", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest());
		doReturn(Lists.list(ericssonNeighbour)).when(dnsFacade).lookupNeighbours();

        neighbourService.incomingCapabilities(ericsson, Collections.emptySet());

		verify(dnsFacade, times(1)).lookupNeighbours();
	}

	@Test
	public void postingCapabilitiesUnknownInDNSReturnsError() {
		// Mock the incoming API object.
		CapabilitiesApi unknownNeighbour = new CapabilitiesApi();
		unknownNeighbour.setName("unknownNeighbour");
		unknownNeighbour.setCapabilities(Collections.singleton(new CapabilityApi(new DatexApplicationApi("", "", "NO", "", List.of(), "", "publisherName"), new MetadataApi())));

		Neighbour ericssonNeighbour = new Neighbour("ericsson", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest());
		doReturn(Lists.list(ericssonNeighbour)).when(dnsFacade).lookupNeighbours();

		Throwable thrown = catchThrowable(() -> neighbourService.incomingCapabilities(unknownNeighbour, Collections.emptySet()));

		assertThat(thrown).isInstanceOf(InterchangeNotInDNSException.class);
		verify(dnsFacade, times(1)).lookupNeighbours();
	}

	@Test
	//TODO IncomingSubscriptionRequest needs to be worked a bit on. Right now, it's difficult to test this method using mocks!
	void postingSubscriptionRequestReturnsStatusRequested() {

		// Create incoming subscription request api objcet
		SubscriptionRequestApi ericsson = new SubscriptionRequestApi("ericsson", Collections.singleton(
				new RequestedSubscriptionApi("originatingCountry = 'FI'")
		));

		// Mock saving Neighbour to Neighbour repository
		NeighbourCapabilities capabilities = new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		NeighbourSubscription firstSubscription = new NeighbourSubscription("originatingCountry = 'FI'", NeighbourSubscriptionStatus.REQUESTED, "ericsson");
		firstSubscription.setId(1);
		firstSubscription.setPath("/ericsson/");
		Set<NeighbourSubscription> subscriptions = Sets.newSet(firstSubscription);
		NeighbourSubscriptionRequest returnedSubscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
		Neighbour updatedNeighbour = new Neighbour("ericsson", capabilities, returnedSubscriptionRequest, null);

		when(neighbourRepository.save(updatedNeighbour)).thenAnswer(
				a -> {
					Object argument = a.getArgument(0);
					Neighbour neighbour = (Neighbour)argument;
					int i = 0;
					for (NeighbourSubscription subscription : neighbour.getNeighbourRequestedSubscriptions().getSubscriptions()) {
						subscription.setId(i);
						subscription.setPath("/ericsson/");
						i++;
					}
					return neighbour;
				}
		);
		doReturn(updatedNeighbour).when(neighbourRepository).findByName(anyString());

		// Mock response from DNS facade on Server
		Neighbour ericssonNeighbour = new Neighbour("ericsson", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest());

		neighbourService.incomingSubscriptionRequest(ericsson);
		verify(neighbourRepository, times(2)).save(any(Neighbour.class)); //saved twice because first save generates id, and second save saves the path derived from the ids
		verify(neighbourRepository, times(1)).findByName(anyString());
		verify(dnsFacade, times(0)).lookupNeighbours();
	}

	@Test
	public void findBouvetExists() {
		when(dnsFacade.lookupNeighbours()).thenReturn(Lists.list(new Neighbour("bouveta-fed.itsinterchange.eu", null, null, null)));
		Neighbour neighbour = neighbourService.findNeighbour("bouveta-fed.itsinterchange.eu");
		assertThat(neighbour).isNotNull();
	}

	@Test
	public void findNotDefinedDoesNotExists() {
		when(dnsFacade.lookupNeighbours()).thenReturn(Lists.list(new Neighbour("bouveta-fed.itsinterchange.eu", null, null, null)));
		Throwable thrown = catchThrowable(() -> neighbourService.findNeighbour("no-such-interchange.itsinterchange.eu"));
		assertThat(thrown).isInstanceOf(InterchangeNotInDNSException.class);
	}

	@Test
	public void findSubscriptionsHappyCase() {
		String neighbourName = "neighbour";
		int id = 1;
		NeighbourSubscription subscription = new NeighbourSubscription();
		subscription.setId(id);
		subscription.setPath("/" + neighbourName + "/subscriptions/" + id);
		subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.REQUESTED);
		subscription.setSelector("originatingCountry = 'NO'");
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		Neighbour neighbour = new Neighbour(neighbourName, new NeighbourCapabilities(), subscriptionRequest,new SubscriptionRequest());
		when(neighbourRepository.findByName(neighbourName)).thenReturn(neighbour);

		SubscriptionResponseApi subscriptions = neighbourService.findSubscriptions(neighbourName);
		assertThat(subscriptions.getName()).isEqualTo(neighbourName);
		assertThat(subscriptions.getSubscriptions()).hasSize(1);

		verify(neighbourRepository, times(1)).findByName(neighbourName);
	}

	@Test
	public void finSubscriptionsWhenNeighbourIsNotKnown() {
		when(neighbourRepository.findByName(any())).thenReturn(null);

		Assertions.assertThrows(InterchangeNotFoundException.class, () ->
				neighbourService.findSubscriptions("nordea"));

	}

	@Test
	public void messageCollectorWillStartAfterCompleteOptimisticControlChannelFlowAndExtraIncomingCapabilityExchange() {
		Neighbour neighbour = new Neighbour("neigbhour-one", "5671");
		when(neighbourRepository.findByName(any())).thenReturn(neighbour);
		neighbourService.incomingCapabilities(new CapabilitiesApi("neighbour-one", Set.of(new CapabilityApi(new DatexApplicationApi("NO-213", "NO-pub", "NO", "1.0", List.of("0122"), "SituationPublication", "publisherName"), new MetadataApi(RedirectStatusApi.OPTIONAL)))), Collections.emptySet());
		verify(neighbourRepository).save(neighbour);
		verify(neighbourRepository, times(1)).findByName("neighbour-one");
	}



	private static Subscription getSubscriptionById(Set<Subscription> ourRequestedSubscriptions, int b) {
		for (Subscription s : ourRequestedSubscriptions) {
			if (Objects.equals(s.getId(), b)) {
				return s;
			}
		}
		return null;
	}





	private NeighbourCapability getDatexNeighbourCapability(String country) {
		return new NeighbourCapability(new DatexApplication(country + "-123", country + "-pub", country, "1.0", List.of("0122"), "SituationPublication", "publisherName"), new Metadata(RedirectStatus.OPTIONAL));
	}
}