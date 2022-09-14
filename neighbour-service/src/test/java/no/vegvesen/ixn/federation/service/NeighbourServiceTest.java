package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.NeighbourDiscovererProperties;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotInDNSException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.assertj.core.util.Lists;
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
	ListenerEndpointRepository listenerEndpointRepository;
	@Mock
	DNSFacade dnsFacade;
	@Mock
	NeighbourFacade neighbourFacade;
	@Mock
	MatchRepository matchRepository;

	private NeighbourDiscovererProperties discovererProperties = new NeighbourDiscovererProperties();
	private GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
	private String myName = "bouvet.itsinterchange.eu";

	NeighbourService neighbourService;
	NeigbourDiscoveryService neigbourDiscoveryService;

	@BeforeEach
	void setUp() {
		InterchangeNodeProperties interchangeNodeProperties = new InterchangeNodeProperties(myName, "5671");
		neighbourService = new NeighbourService(neighbourRepository, dnsFacade,interchangeNodeProperties);
		neigbourDiscoveryService = new NeigbourDiscoveryService(dnsFacade,neighbourRepository,listenerEndpointRepository,interchangeNodeProperties,backoffProperties,discovererProperties, matchRepository);
	}

	@Test
	void isAutowired() {
		assertThat(neighbourService).isNotNull();
	}


	@Test
	void postDatexDataTypeCapability() {
		CapabilitiesApi ericsson = new CapabilitiesApi();
		ericsson.setName("ericsson");
		CapabilityApi ericssonDataType = new DatexCapabilityApi("myPublisherId", "NO", null, Sets.newSet(), Sets.newSet("myPublicationType"));
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Mock dns lookup
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
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
		CapabilityApi ericssonDataType = new DatexCapabilityApi("NO");
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Mock dns lookup
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		doReturn(Lists.list(ericssonNeighbour)).when(dnsFacade).lookupNeighbours();

        neighbourService.incomingCapabilities(ericsson, Collections.emptySet());

		verify(dnsFacade, times(1)).lookupNeighbours();
	}

	@Test
	public void postingCapabilitiesUnknownInDNSReturnsError() {
		// Mock the incoming API object.
		CapabilitiesApi unknownNeighbour = new CapabilitiesApi();
		unknownNeighbour.setName("unknownNeighbour");
		unknownNeighbour.setCapabilities(Collections.singleton(new DatexCapabilityApi("NO")));

		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
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
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		NeighbourSubscription firstSubscription = new NeighbourSubscription("originatingCountry = 'FI'", SubscriptionStatus.REQUESTED, "ericsson");
		firstSubscription.setId(1);
		firstSubscription.setPath("/ericsson/");
		Set<NeighbourSubscription> subscriptions = Sets.newSet(firstSubscription);
		NeighbourSubscriptionRequest returnedSubscriptionRequest = new NeighbourSubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
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
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");

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
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);
		subscription.setSelector("originatingCountry = 'NO'");
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		Neighbour neighbour = new Neighbour(neighbourName, new Capabilities(), subscriptionRequest,new SubscriptionRequest());
		when(neighbourRepository.findByName(neighbourName)).thenReturn(neighbour);

		SubscriptionResponseApi subscriptions = neighbourService.findSubscriptions(neighbourName);
		assertThat(subscriptions.getName()).isEqualTo(neighbourName);
		assertThat(subscriptions.getSubscriptions()).hasSize(1);

		verify(neighbourRepository, times(1)).findByName(neighbourName);
	}

	@Test
	public void noSubscriptionsAreAddedWhenLocalSubscriptionsAndCapabilitiesAreTheSame() {
		Set<LocalSubscription> localSubscriptions = new HashSet<>();
		localSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' AND originatingCountry = 'NO'", myName));

		Subscription subscription = new Subscription(1, SubscriptionStatus.ACCEPTED, "messageType = 'DATEX2' AND originatingCountry = 'NO'", "/neighbour/subscriptions/1", myName);

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));

		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(getDatexCapability("NO")));
		Neighbour neighbour = new Neighbour("neighbour", capabilities, new NeighbourSubscriptionRequest(), subscriptionRequest);

		neigbourDiscoveryService.postSubscriptionRequest(neighbour, localSubscriptions, neighbourFacade);
		verify(neighbourFacade, times(0)).postSubscriptionRequest(any(Neighbour.class), any(), any(String.class));
	}

	@Test
	public void subscriptionsAreAddedWhenLocalSubscriptionsAndCapabilitiesAreNotTheSame() {
		Set<LocalSubscription> localSubscriptions = new HashSet<>();
		localSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' AND originatingCountry = 'NO'"));
		localSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' AND originatingCountry = 'SE'"));

		Subscription subscription1 = new Subscription(1, SubscriptionStatus.ACCEPTED, "messageType = 'DATEX2' AND originatingCountry = 'NO'", "/neighbour/subscriptions/1", "self");

		SubscriptionRequest existingSubscriptions = new SubscriptionRequest();
		existingSubscriptions.setSubscriptions(Collections.singleton(subscription1));

		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newSet(getDatexCapability("NO"), getDatexCapability("SE")));
		Neighbour neighbour = new Neighbour("neighbour", capabilities, new NeighbourSubscriptionRequest(), existingSubscriptions);

		Subscription subscription2 = new Subscription(2, SubscriptionStatus.ACCEPTED, "messageType = 'DATEX2' AND originatingCountry = 'SE'", "/neighbour/subscriptions/2", "self");

		when(neighbourFacade.postSubscriptionRequest(any(), any(), any())).thenReturn(new HashSet<>(Collections.singleton(subscription2)));
		when(neighbourRepository.save(neighbour)).thenReturn(neighbour);
		neigbourDiscoveryService.postSubscriptionRequest(neighbour, localSubscriptions, neighbourFacade);
		verify(neighbourFacade, times(1)).postSubscriptionRequest(any(Neighbour.class), any(), any(String.class));
		assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptions()).hasSize(2);
	}

	@Test
	public void subscriptionsAreRemovedWhenLocalSubscriptionsAndCapabilitiesAreNotTheSame() {
		Set<LocalSubscription> localSubscriptions = new HashSet<>();
		localSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' AND originatingCountry = 'NO'"));

		Subscription subscription1 = new Subscription(1, SubscriptionStatus.ACCEPTED, "messageType = 'DATEX2' AND originatingCountry = 'NO'", "/localnode/subscriptions/1", "localnode");
		Subscription subscription2 = new Subscription(2, SubscriptionStatus.ACCEPTED, "messageType = 'DATEX2' AND originatingCountry = 'SE'", "/localnode/subscriptions/2", "localnode");

		SubscriptionRequest existingSubscriptions = new SubscriptionRequest();
		existingSubscriptions.setSubscriptions(new HashSet<>(Arrays.asList(subscription1, subscription2)));

		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newSet(getDatexCapability("NO"), getDatexCapability("SE")));
		Neighbour neighbour = new Neighbour("neighbour", capabilities, new NeighbourSubscriptionRequest(), existingSubscriptions);

		when(neighbourRepository.save(neighbour)).thenReturn(neighbour);
		neigbourDiscoveryService.postSubscriptionRequest(neighbour, localSubscriptions, neighbourFacade);
		//TODO should actually post a subscription request with only one subscription here????
		//verify(neighbourFacade, times(0)).postSubscriptionRequest(any(Neighbour.class), any(), any(String.class));
		//So here, the neighbour should have 2 subscriptions, one TEAR_DOWN and one in another state
		assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptionById(2).getSubscriptionStatus()).isEqualTo(SubscriptionStatus.TEAR_DOWN);
	}

	@Test
	public void subscriptionsAreAddedAndRemovedWhenLocalSubscriptionsAndCapabilitiesAreNotTheSame() {
		Set<LocalSubscription> localSubscriptions = new HashSet<>();
		localSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' AND originatingCountry = 'NO'"));
		localSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' AND originatingCountry = 'FI'"));

		Subscription subscription1 = new Subscription(1, SubscriptionStatus.ACCEPTED, "messageType = 'DATEX2' AND originatingCountry = 'NO'", "/neighbour/subscriptions/1", "self");
		Subscription subscription2 = new Subscription(2, SubscriptionStatus.ACCEPTED, "messageType = 'DATEX2' AND originatingCountry = 'SE'", "/neighbour/subscriptions/2", "self");
		Subscription subscription3 = new Subscription(3, SubscriptionStatus.ACCEPTED, "messageType = 'DATEX2' AND originatingCountry = 'FI'", "/neighbour/subscriptions/3", "self");

		SubscriptionRequest existingSubscriptions = new SubscriptionRequest();
		existingSubscriptions.setSubscriptions(new HashSet<>(Arrays.asList(subscription1, subscription2)));

		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newSet(getDatexCapability("NO"), getDatexCapability("SE"), getDatexCapability("FI")));
		Neighbour neighbour = new Neighbour("neighbour", capabilities, new NeighbourSubscriptionRequest(), existingSubscriptions);

		when(neighbourFacade.postSubscriptionRequest(any(), any(), any())).thenReturn(new HashSet<>(Collections.singleton(subscription3)));
		when(neighbourRepository.save(neighbour)).thenReturn(neighbour);
		neigbourDiscoveryService.postSubscriptionRequest(neighbour, localSubscriptions, neighbourFacade);
		verify(neighbourFacade, times(1)).postSubscriptionRequest(any(Neighbour.class), any(), any(String.class));
		assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptionById(2).getSubscriptionStatus()).isEqualTo(SubscriptionStatus.TEAR_DOWN);
		assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptionById(3).getSubscriptionStatus()).isEqualTo(SubscriptionStatus.ACCEPTED);
	}

	@Test
	public void listenerEndpointsAreSavedFromEndpointsList() {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("my-neighbour");

		Endpoint endpoint1 = new Endpoint("my-source-1", "host-1", 5671);
		Endpoint endpoint2 = new Endpoint("my-source-2", "host-2", 5671);

		Set<Endpoint> endpoints = new HashSet<>(Sets.newSet(endpoint1, endpoint2));

		when(listenerEndpointRepository.findByNeighbourNameAndHostAndPortAndSource("my-neighbour", "host-1", 5671, "my-source-1")).thenReturn(null);
		when(listenerEndpointRepository.findByNeighbourNameAndHostAndPortAndSource("my-neighbour", "host-2", 5671, "my-source-2")).thenReturn(null);

		ListenerEndpoint listenerEndpoint1 = new ListenerEndpoint("my-neighbour", "my-source-1", "host-1", 5671, new Connection());
		ListenerEndpoint listenerEndpoint2 = new ListenerEndpoint("my-neighbour", "my-source-2", "host-2", 5671, new Connection());

		when(listenerEndpointRepository.save(listenerEndpoint1)).thenReturn(listenerEndpoint1);
		when(listenerEndpointRepository.save(listenerEndpoint2)).thenReturn(listenerEndpoint2);

		neigbourDiscoveryService.createListenerEndpointFromEndpointsList(neighbour, endpoints, "");

		verify(listenerEndpointRepository, times(2)).save(any(ListenerEndpoint.class));
	}

	@Test
	public void listenerEndpointsAreRemoverFromEndpointsList() {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("my-neighbour");

		Endpoint endpoint1 = new Endpoint("my-source-1", "my-endpoint-1", 5671);
		Endpoint endpoint2 = new Endpoint("my-source-2", "my-endpoint-2", 5671);

		Set<Endpoint> endpoints = new HashSet<>(Sets.newSet(endpoint1, endpoint2));

		ListenerEndpoint listenerEndpoint1 = new ListenerEndpoint("my-neighbour", "my-source-1", "my-endpoint-1", 5671, new Connection());
		ListenerEndpoint listenerEndpoint2 = new ListenerEndpoint("my-neighbour", "my-source-2", "my-endpoint-1", 5671,  new Connection());

		when(listenerEndpointRepository.findByNeighbourNameAndHostAndPortAndSource("my-neighbour", "my-endpoint-1", 5671, "my-source-1")).thenReturn(listenerEndpoint1);
		when(listenerEndpointRepository.findByNeighbourNameAndHostAndPortAndSource("my-neighbour", "my-endpoint-2", 5671, "my-source-2")).thenReturn(listenerEndpoint2);

		neigbourDiscoveryService.tearDownListenerEndpointsFromEndpointsList(neighbour, endpoints);

		verify(listenerEndpointRepository, times(2)).delete(any(ListenerEndpoint.class));
	}

	private Capability getDatexCapability(String country) {
		return new DatexCapability(null, country, null, null, null);
	}

}