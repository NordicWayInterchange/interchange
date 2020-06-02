package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.discoverer.NeighbourDiscovererProperties;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourRESTFacade;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.onboard.SelfService;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class NeighbourServiceDiscoveryTest {

	// Mocks
	private NeighbourRepository neighbourRepository;
	private DNSFacade dnsFacade;
	private NeighbourRESTFacade neighbourRESTFacade;
	private NeighbourDiscovererProperties discovererProperties;
	private SelfService selfService;

	private GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

	private String myName = "bouvet.itsinterchange.eu";

	private NeighbourService neighbourService;

	// Objects used in testing
	private DataType datexNo = getDatexNoDataType();
	private Self self;

	private DataType getDatexNoDataType() {
		Map<String, String> datexDataTypeHeaders = new HashMap<>();
		datexDataTypeHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		datexDataTypeHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		return new DataType(datexDataTypeHeaders);
	}

	@BeforeEach
	public void before(){
		self = createSelf();
		neighbourRepository = mock(NeighbourRepository.class);
		dnsFacade = mock(DNSFacade.class);
		neighbourRESTFacade = mock(NeighbourRESTFacade.class);
		discovererProperties = mock(NeighbourDiscovererProperties.class);
		selfService = mock(SelfService.class);
		neighbourService = new NeighbourService(neighbourRepository, dnsFacade, backoffProperties, discovererProperties, neighbourRESTFacade, myName, selfService);
	}

	private Neighbour createNeighbour() {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("ericsson.itsinterchange.eu");
		neighbour.setControlChannelPort("8080");
		return neighbour;
	}

	private Self createSelf() {
		// Self setup
		self = new Self(myName);
		Set<DataType> selfCapabilities = Collections.singleton(getDatexNoDataType());
		self.setLocalCapabilities(selfCapabilities);
		Set<DataType> selfSubscriptions = getDataTypeSetOriginatingCountry("FI");
		self.setLocalSubscriptions(selfSubscriptions);
		return self;
	}

	private Set<DataType> getDataTypeSetOriginatingCountry(String country) {
		return Sets.newLinkedHashSet(new DataType(Maps.newHashMap(MessageProperty.ORIGINATING_COUNTRY.getName(), country)));
	}

	@Test
	public void testNewNeighbourIsAddedToDatabase(){
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(createNeighbour()));
		when(neighbourRepository.findByName(any(String.class))).thenReturn(null);
		when(neighbourRepository.save(any(Neighbour.class))).thenReturn(mock(Neighbour.class));

		neighbourService.checkForNewNeighbours();

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void testKnownNeighbourIsNotAddedToDatabase(){
		Neighbour ericsson = createNeighbour();
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(ericsson));
		when(neighbourRepository.findByName(any(String.class))).thenReturn(ericsson);

		neighbourService.checkForNewNeighbours();

		verify(neighbourRepository, times(0)).save(any(Neighbour.class));
	}

	@Test
	public void testNeighbourWithSameNameAsUsIsNotSaved(){
		// Mocking finding ourselves in the DNS lookup.
		Neighbour discoveringNode = new Neighbour();
		discoveringNode.setName(self.getName());
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(discoveringNode));

		neighbourService.checkForNewNeighbours();

		verify(neighbourRepository,times(1)).findByName(myName);
		verify(neighbourRepository, times(0)).save(any(Neighbour.class));
	}

	//TODO Make a test for having a Neighbour that is in the database, but not in DNS (in other words, removed).

	/**
	 * Capabilities exchange and subscription request
	 */

	@Test
	public void successfulCapabilitiesPostCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		Capabilities capabilities = new Capabilities();
		assertThat(capabilities.getDataTypes()).isEmpty();

		doReturn(capabilities).when(neighbourRESTFacade).postCapabilitiesToCapabilities(any(Self.class), any(Neighbour.class));
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));


		doReturn(self).when(selfService).fetchSelf();

		neighbourService.capabilityExchange(Collections.singletonList(ericsson));

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void successfulSubscriptionRequestCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		doReturn(createFirstSubscriptionRequestResponse()).when(neighbourRESTFacade).postSubscriptionRequest(any(Self.class), any(Neighbour.class),anySet());
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		ericsson.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, getDataTypeSet("FI")));

		doReturn(self).when(selfService).fetchSelf();

		neighbourService.evaluateAndPostSubscriptionRequest(Collections.singletonList(ericsson));

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	private Set<DataType> getDataTypeSet(String originatingCountry) {
		return Sets.newLinkedHashSet(new DataType(1, MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry));
	}

	private SubscriptionRequest createFirstSubscriptionRequestResponse() {
		Subscription firstSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);
		return new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
	}


	@Test
	public void gracefulBackoffPostOfCapabilityDoesNotHappenBeforeAllowedPostTime(){
		Neighbour ericsson = createNeighbour();
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		ericsson.setBackoffStart(futureTime);
		ericsson.setConnectionStatus(ConnectionStatus.FAILED);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(a -> a.getArgument(0));

		neighbourService.capabilityExchange(Collections.singletonList(ericsson));

		verify(neighbourRESTFacade, times(0)).postCapabilitiesToCapabilities(any(Self.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestDoesNotHappenBeforeAllowedTime(){
		Neighbour ericsson = createNeighbour();
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		ericsson.setBackoffStart(futureTime);
		ericsson.setConnectionStatus(ConnectionStatus.FAILED);
		ericsson.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, getDataTypeSetOriginatingCountry("FI")));
		doReturn(self).when(selfService).fetchSelf();
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(a -> a.getArgument(0));

		neighbourService.evaluateAndPostSubscriptionRequest(Collections.singletonList(ericsson));

		verify(neighbourRESTFacade, times(0)).postSubscriptionRequest(any(Self.class), any(Neighbour.class), any());
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionDoesNotHappenBeforeAllowedTime(){
		Neighbour ericsson = createNeighbour();
		// Neighbour ericsson has subscription to poll
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));
		// Setting up Ericsson's failed subscriptions
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED);
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(ericssonSubscription));
		ericsson.setFedIn(subReq);
		ericsson.setBackoffStart(LocalDateTime.now().plusSeconds(10));
		neighbourService.pollSubscriptions();
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		verify(neighbourRESTFacade, times(0)).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPostOfCapabilitiesHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();

		DataType firstDataType = this.datexNo;
		Capabilities ericssonCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(firstDataType));

		doReturn(ericssonCapabilities).when(neighbourRESTFacade).postCapabilitiesToCapabilities(any(Self.class),any(Neighbour.class));

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.setBackoffStart(pastTime);
		doReturn(self).when(selfService).fetchSelf();
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(p -> p.getArgument(0));

		neighbourService.capabilityExchange(Collections.singletonList(ericsson));

		verify(neighbourRESTFacade, times(1)).postCapabilitiesToCapabilities(any(Self.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();
		doReturn(createFirstSubscriptionRequestResponse()).when(neighbourRESTFacade).postSubscriptionRequest(any(Self.class), any(Neighbour.class),anySet());
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.setBackoffStart(pastTime);
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		doReturn(self).when(selfService).fetchSelf();
		ericsson.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, getDataTypeSetOriginatingCountry("FI")));
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(a -> a.getArgument(0));

		neighbourService.evaluateAndPostSubscriptionRequest(Collections.singletonList(ericsson));

		verify(neighbourRESTFacade, times(1)).postSubscriptionRequest(any(Self.class), any(Neighbour.class),anySet());
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();
		// Return an Neighbour with a subscription to poll.
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));

		// Mock result of polling in backoff.
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED);
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(ericssonSubscription));
		ericsson.setFedIn(subReq);
		doReturn(ericssonSubscription).when(neighbourRESTFacade).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.setBackoffStart(pastTime);
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions();

		verify(neighbourRESTFacade, times(1)).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void failedPostOfCapabilitiesInBackoffIncreasesNumberOfBackoffAttempts(){
		Neighbour ericsson = createNeighbour();
		ericsson.setBackoffAttempts(0);
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(5);

		ericsson.setBackoffStart(pastTime);
		Mockito.doThrow(new CapabilityPostException("Exception from mock")).when(neighbourRESTFacade).postCapabilitiesToCapabilities(any(Self.class), any(Neighbour.class));
		doReturn(self).when(selfService).fetchSelf();
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(p -> p.getArgument(0));

		neighbourService.capabilityExchange(Collections.singletonList(ericsson));

		assertThat(ericsson.getBackoffAttempts()).isEqualTo(1);
	}

	@Test
	public void unsuccessfulPostOfCapabilitiesToNodeWithTooManyPreviousBackoffAttemptsMarksCapabilitiesUnreachable(){
		Neighbour ericsson = createNeighbour();
		ericsson.setBackoffAttempts(4);

		Capabilities ericssonCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.FAILED, Collections.singleton(datexNo));
		ericsson.setCapabilities(ericssonCapabilities);

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.setBackoffStart(pastTime);
		doThrow(new CapabilityPostException("Exception from mock")).when(neighbourRESTFacade).postCapabilitiesToCapabilities(any(Self.class), any(Neighbour.class));

		doReturn(self).when(selfService).fetchSelf();
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(p -> p.getArgument(0));

		neighbourService.capabilityExchange(Collections.singletonList(ericsson));

		assertThat(ericsson.getConnectionStatus()).isEqualTo(ConnectionStatus.UNREACHABLE);
		verify(neighbourRESTFacade).postCapabilitiesToCapabilities(any(Self.class),any(Neighbour.class));

	}

	/*
		Subscription polling tests
	 */

	@Test
	public void successfulPollOfSubscriptionCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);
		SubscriptionRequest ericssonSubscription = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		ericsson.setFedIn(ericssonSubscription);
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(ericsson));

		Subscription polledSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED);
		when(neighbourRESTFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(polledSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions();

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void allSubscriptionsHaveFinalStatusFlipsFedInStatusToEstablished(){

		Neighbour spyNeighbour = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour.setFedIn(subscriptionRequest);
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED);
		when(neighbourRESTFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions();

		assertThat(spyNeighbour.getFedIn().getSubscriptions()).contains(createdSubscription);
	}

	@Test
	public void allSubscriptionsRejectedFlipsFedInStatusToRejected(){
		Neighbour spyNeighbour = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour.setFedIn(subscriptionRequest);
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REJECTED);
		when(neighbourRESTFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions();

		assertThat(spyNeighbour.getFedIn().getAcceptedSubscriptions()).isEmpty();
	}

	@Test
	public void subscriptionStatusAcceptedKeepsFedInStatusRequested(){
		Neighbour spyNeighbour = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour.setFedIn(subscriptionRequest);

		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus.REQUESTED, SubscriptionStatus.ACCEPTED)).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED);
		when(neighbourRESTFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourService.pollSubscriptions();

		assertThat(spyNeighbour.getFedIn().getStatus()).isEqualTo(SubscriptionRequestStatus.REQUESTED);
	}

	//TODO: test why local subscriptions for self must be different from subscription request for neighbours
	// Only capabilities of neighbour and subscription of self is relevant for matching
	// To introduce error make sure part 1 and part 2 below has same selector filter
	@Test
	public void subscriptionRequestProcessesAllNeighboursDespiteNetworkError() {
		HashSet<DataType> dtNO = new HashSet<>();
		dtNO.add(getDatexNoDataType());
		Capabilities capabilitiesNO = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, dtNO);
		Neighbour neighbourA = new Neighbour("a", capabilitiesNO, getEmptySR(), getEmptySR());
		Neighbour neighbourB = new Neighbour("b", capabilitiesNO, getEmptySR(), getEmptySR());
		Neighbour neighbourC = new Neighbour("c", capabilitiesNO, getEmptySR(), getEmptySR());

		List<Neighbour> neighbours = new LinkedList<>();
		neighbours.add(neighbourA);
		neighbours.add(neighbourB);
		neighbours.add(neighbourC);


		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in

		when(neighbourRESTFacade.postSubscriptionRequest(any(Self.class), eq(neighbourA),anySet())).thenReturn(getReturnedSubscriptionRequest());
		when(neighbourRESTFacade.postSubscriptionRequest(any(Self.class), eq(neighbourB),anySet())).thenThrow(new SubscriptionRequestException("time-out"));
		when(neighbourRESTFacade.postSubscriptionRequest(any(Self.class), eq(neighbourC),anySet())).thenReturn(getReturnedSubscriptionRequest());

		// Self setup
		Self discoveringNode = new Self("d");
		Set<DataType> selfCapabilities = new HashSet<>();
		selfCapabilities.add(getDatexNoDataType());
		discoveringNode.setLocalCapabilities(selfCapabilities);

		Set<DataType> selfSubscriptions = getDataTypeSetOriginatingCountry("NO");

		discoveringNode.setLocalSubscriptions(selfSubscriptions);
		when(selfService.fetchSelf()).thenReturn(discoveringNode);

		neighbourService.evaluateAndPostSubscriptionRequest(neighbours);

		verify(neighbourRESTFacade, times(3)).postSubscriptionRequest(any(Self.class), any(Neighbour.class),anySet());
	}

	private SubscriptionRequest getEmptySR() {
		Set<Subscription> subscription = new HashSet<>();
		return new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, subscription);
	}

	private SubscriptionRequest getReturnedSubscriptionRequest() {
		HashSet<Subscription> returnedSubscriptionsFromNeighbour = new HashSet<>();
		returnedSubscriptionsFromNeighbour.add(new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED));

		return new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, returnedSubscriptionsFromNeighbour);
	}

	@Test
	public void calculatedSubscriptionRequestSameAsNeighbourSubscriptionsAllowsNextNeighbourToBeSaved() {
		Self self = new Self("self");
		Set<DataType> selfLocalSubscriptions = getDataTypeSetOriginatingCountry("NO");
		self.setLocalSubscriptions(selfLocalSubscriptions);

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.emptySet());
		DataType neighbourDataType = getDatexNoDataType();
		Set<DataType> dataTypeSet = new HashSet<>();
		dataTypeSet.add(neighbourDataType);
		Capabilities neighbourCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,dataTypeSet);
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilities,subscriptionRequest,new SubscriptionRequest());
		Set<Subscription> neighbourFedInSubscription = new HashSet<>();
		neighbourFedInSubscription.add(new Subscription("originatingCountry = 'NO'",SubscriptionStatus.ACCEPTED));
		neighbour.setFedIn(new SubscriptionRequest(null,neighbourFedInSubscription));

		assertThat(neighbour.hasEstablishedSubscriptions()).isTrue();
		Set<Subscription> subscriptions = self.calculateCustomSubscriptionForNeighbour(neighbour);
		assertThat(subscriptions.isEmpty()).isFalse();
		assertThat(neighbour.getFedIn().getSubscriptions()).isEqualTo(subscriptions);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in

		Neighbour otherNeighbour = new Neighbour("otherNeighbour",
				new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, getDataTypeSet("NO")),
				new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED,Collections.emptySet()),
				new SubscriptionRequest());

		doReturn(self).when(selfService).fetchSelf();
		when(neighbourRESTFacade.postSubscriptionRequest(any(), any(), any())).thenReturn(mock(SubscriptionRequest.class));

		neighbourService.evaluateAndPostSubscriptionRequest(Arrays.asList(neighbour,otherNeighbour));

		verify(neighbourRepository).save(otherNeighbour);
    }

	@Test
	void unreachableNeighboursWillReceiveCapabilityExchangeWhenRetryingUnreachableNeighbours() {
		Neighbour n1 = new Neighbour();
		n1.setName("neighbour-one");
		n1.setConnectionStatus(ConnectionStatus.UNREACHABLE);
		n1.setBackoffStart(LocalDateTime.now().minusDays(2));

		Neighbour n2 = new Neighbour();
		n2.setName("neighbour-two");
		n2.setConnectionStatus(ConnectionStatus.UNREACHABLE);
		n2.setBackoffStart(LocalDateTime.now().minusDays(2));

		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in
		when(neighbourRepository.findByConnectionStatus(ConnectionStatus.UNREACHABLE)).thenReturn(Lists.list(n1, n2));

		neighbourService.retryUnreachable();

		verify(neighbourRESTFacade, times(2)).postCapabilitiesToCapabilities(any(), any());
	}
}
