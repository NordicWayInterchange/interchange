package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.discoverer.NeighbourDiscovererProperties;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotInDNSException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Self;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.onboard.SelfService;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NeighbourServiceTest {
	@Mock
	NeighbourRepository neighbourRepository;
	@Mock
	ListenerEndpointRepository listenerEndpointRepository;
	@Mock
	DNSFacade dnsFacade;
	@Mock
	SelfService selfService;
	@Mock
	NeighbourFacade neighbourFacade;


	private NeighbourDiscovererProperties discovererProperties = new NeighbourDiscovererProperties();
	private GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
	private String myName = "bouvet.itsinterchange.eu";

	NeighbourService neighbourService;

	@BeforeEach
	void setUp() {
		neighbourService = new NeighbourService(neighbourRepository, dnsFacade, backoffProperties, discovererProperties, new InterchangeNodeProperties(myName, "5671"), listenerEndpointRepository);
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
		Mockito.doReturn(Lists.list(ericssonNeighbour)).when(dnsFacade).lookupNeighbours();

		CapabilityApi response = neighbourService.incomingCapabilities(ericsson, new Self("bouvet"));

		Mockito.verify(dnsFacade, Mockito.times(1)).lookupNeighbours();
		Mockito.verify(neighbourRepository, Mockito.times(1)).save(ArgumentMatchers.any(Neighbour.class));
		Assertions.assertThat(response.getName()).isEqualTo("bouvet");
	}

	@Test
	void postingSubscriptionRequestFromUnseenNeighbourReturnsException() {
		// Create incoming subscription request api objcet
		SubscriptionRequestApi ericsson = new SubscriptionRequestApi("ericsson",Collections.singleton(
				new RequestedSubscriptionApi("originatingCountry = 'FI'")
		));

		// Mock saving Neighbour to Neighbour repository
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		Subscription firstSubscription = new Subscription("originatingCountry = 'FI'", SubscriptionStatus.REQUESTED);
		firstSubscription.setPath("/ericsson/subscriptions/1");
		SubscriptionRequest returnedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		Neighbour updatedNeighbour = new Neighbour("ericsson", capabilities, returnedSubscriptionRequest, null);

		// Mock response from DNS facade on Server
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");

		Throwable thrown = Assertions.catchThrowable(() -> neighbourService.incomingSubscriptionRequest(ericsson));

		Assertions.assertThat(thrown).isInstanceOf(SubscriptionRequestException.class);
		Mockito.verify(neighbourRepository, Mockito.times(1)).findByName(ArgumentMatchers.anyString());
		Mockito.verify(dnsFacade, Mockito.times(0)).lookupNeighbours();
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
		Mockito.doReturn(Lists.list(ericssonNeighbour)).when(dnsFacade).lookupNeighbours();

		neighbourService.incomingCapabilities(ericsson, new Self(myName));

		Mockito.verify(dnsFacade, Mockito.times(1)).lookupNeighbours();
	}

	@Test
	public void postingCapabilitiesUnknownInDNSReturnsError() {
		// Mock the incoming API object.
		CapabilityApi unknownNeighbour = new CapabilityApi();
		unknownNeighbour.setName("unknownNeighbour");
		unknownNeighbour.setCapabilities(Collections.singleton(new Datex2DataTypeApi("NO")));

		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		Mockito.doReturn(Lists.list(ericssonNeighbour)).when(dnsFacade).lookupNeighbours();

		Throwable thrown = Assertions.catchThrowable(() -> neighbourService.incomingCapabilities(unknownNeighbour, new Self("some-node-name")));

		Assertions.assertThat(thrown).isInstanceOf(InterchangeNotInDNSException.class);
		Mockito.verify(dnsFacade, Mockito.times(1)).lookupNeighbours();
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
		Subscription firstSubscription = new Subscription("originatingCountry = 'FI'", SubscriptionStatus.REQUESTED);
		firstSubscription.setId(1);
		//firstSubscription.setPath("/ericsson/subscriptions/1");
		Set<Subscription> subscriptions = Sets.newLinkedHashSet(firstSubscription);
		SubscriptionRequest returnedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour updatedNeighbour = new Neighbour("ericsson", capabilities, returnedSubscriptionRequest, null);

		//Mockito.doReturn(updatedNeighbour).when(neighbourRepository).save(ArgumentMatchers.any(Neighbour.class));
		Mockito.when(neighbourRepository.save(updatedNeighbour)).thenAnswer(
				a -> {
					Object argument = a.getArgument(0);
					Neighbour neighbour = (Neighbour)argument;
					int i = 0;
					for (Subscription subscription : neighbour.getNeighbourRequestedSubscriptions().getSubscriptions()) {
						subscription.setId(i);
						i++;
					}
					return neighbour;
				}
		);
		Mockito.doReturn(updatedNeighbour).when(neighbourRepository).findByName(ArgumentMatchers.anyString());

		// Mock response from DNS facade on Server
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");

		neighbourService.incomingSubscriptionRequest(ericsson);
		Mockito.verify(neighbourRepository, Mockito.times(2)).save(ArgumentMatchers.any(Neighbour.class)); //saved twice because first save generates id, and second save saves the path derived from the ids
		Mockito.verify(neighbourRepository, Mockito.times(1)).findByName(ArgumentMatchers.anyString());
		Mockito.verify(dnsFacade, Mockito.times(0)).lookupNeighbours();
	}

	@Test
	public void findBouvetExists() {
		Mockito.when(dnsFacade.lookupNeighbours()).thenReturn(Lists.list(new Neighbour("bouveta-fed.itsinterchange.eu", null, null, null)));
		Neighbour neighbour = neighbourService.findNeighbour("bouveta-fed.itsinterchange.eu");
		Assertions.assertThat(neighbour).isNotNull();
	}

	@Test
	public void findNotDefinedDoesNotExists() {
		Mockito.when(dnsFacade.lookupNeighbours()).thenReturn(Lists.list(new Neighbour("bouveta-fed.itsinterchange.eu", null, null, null)));
		Throwable trown = Assertions.catchThrowable(() -> neighbourService.findNeighbour("no-such-interchange.itsinterchange.eu"));
		Assertions.assertThat(trown).isInstanceOf(InterchangeNotInDNSException.class);
	}

	@Test
	public void calculateCustomSubscriptionForNeighbour_localSubscriptionOriginatingCountryMatchesCapabilityOfNeighbourGivesLocalSubscription() {
		Set<DataType> localSubscriptions = getDataTypeSetOriginatingCountry("NO");

		Capabilities neighbourCapabilitiesDatexNo = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Sets.newLinkedHashSet(getDatex2NorwayDataType()));
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilitiesDatexNo, new SubscriptionRequest(), new SubscriptionRequest());
		Set<Subscription> calculatedSubscription = neighbourService.calculateCustomSubscriptionForNeighbour(neighbour, localSubscriptions);

		assertThat(calculatedSubscription).hasSize(1);
		assertThat(calculatedSubscription.iterator().next().getSelector()).isEqualTo("originatingCountry = 'NO'");
	}

	@Test
	public void calculateCustomSubscriptionForNeighbour_localSubscriptionMessageTypeAndOriginatingCountryMatchesCapabilityOfNeighbourGivesLocalSubscription() {
		Set<DataType> localSubscriptions = new HashSet<>();
		localSubscriptions.add(getDatex2NorwayDataType());

		Capabilities neighbourCapabilitiesDatexNo = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Sets.newLinkedHashSet(getDatex2NorwayDataType()));
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilitiesDatexNo, new SubscriptionRequest(), new SubscriptionRequest());
		Set<Subscription> calculatedSubscription = neighbourService.calculateCustomSubscriptionForNeighbour(neighbour, localSubscriptions);

		assertThat(calculatedSubscription).hasSize(1);
		assertThat(calculatedSubscription.iterator().next().getSelector())
				.contains("originatingCountry = 'NO'")
				.contains(" AND ")
				.contains("messageType = 'DATEX2'");
	}

	@Test
	public void calculateCustomSubscriptionForNeighbour_emptyLocalSubscriptionGivesEmptySet() {
		Self selfWithNoSubscriptions = new Self("self");
		Capabilities neighbourCapabilitiesDatexNo = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Sets.newLinkedHashSet(getDatex2NorwayDataType()));
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilitiesDatexNo, new SubscriptionRequest(), new SubscriptionRequest());
		Set<Subscription> calculatedSubscription = neighbourService.calculateCustomSubscriptionForNeighbour(neighbour, selfWithNoSubscriptions.getLocalSubscriptions());
		assertThat(calculatedSubscription).hasSize(0);
	}

	@Test
	public void findSubscriptionsHappyCase() {
		String neighbourName = "neighbour";
		int id = 1;
		Subscription subscription = new Subscription();
		subscription.setId(id);
		subscription.setPath("/" + neighbourName + "/subscriptions/" + id);
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);
		subscription.setSelector("originatingCountry = 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		Neighbour neighbour = new Neighbour(neighbourName, new Capabilities(), subscriptionRequest,new SubscriptionRequest());
		Mockito.when(neighbourRepository.findByName(neighbourName)).thenReturn(neighbour);

		SubscriptionResponseApi subscriptions = neighbourService.findSubscriptions(neighbourName);
		assertThat(subscriptions.getName()).isEqualTo(neighbourName);
		assertThat(subscriptions.getSubscriptions()).hasSize(1);

		Mockito.verify(neighbourRepository,Mockito.times(1)).findByName(neighbourName);
	}


	private Set<DataType> getDataTypeSetOriginatingCountry(String country) {
		return Sets.newLinkedHashSet(new DataType(Maps.newHashMap(MessageProperty.ORIGINATING_COUNTRY.getName(), country)));
	}

	private DataType getDatex2NorwayDataType() {
		Map<String, String> datexDataTypeHeaders = new HashMap<>();
		datexDataTypeHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		datexDataTypeHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		return new DataType(datexDataTypeHeaders);
	}

}