package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.DiscoveryStateRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;

public class NeighbourDiscovererTest {

	// Mocks
	private NeighbourRepository neighbourRepository;
	private DNSFacade dnsFacade;
	private NeighbourRESTFacade neighbourRESTFacade;
	private NeighbourDiscovererProperties discovererProperties;
	private SelfRepository selfRepository;
	private DiscoveryStateRepository discoveryStateRepository;
	private GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

	private String myName = "bouvet.itsinterchange.eu";

	private NeighbourDiscoverer neighbourDiscoverer;

	// Objects used in testing
	private DataType datexNo = getDatexNoDataType();
	private Self self;

	private DataType getDatexNoDataType() {
		Map<String, String> datexDataTypeHeaders = new HashMap<>();
		datexDataTypeHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		datexDataTypeHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		return new DataType(datexDataTypeHeaders);
	}


	@Before
	public void before(){
		self = createSelf();
		neighbourRepository = mock(NeighbourRepository.class);
		dnsFacade = mock(DNSFacade.class);
		neighbourRESTFacade = mock(NeighbourRESTFacade.class);
		discovererProperties = mock(NeighbourDiscovererProperties.class);
		selfRepository = mock(SelfRepository.class);
		discoveryStateRepository = mock(DiscoveryStateRepository.class);
		neighbourDiscoverer = new NeighbourDiscoverer(dnsFacade,
				neighbourRepository,
				selfRepository,
				discoveryStateRepository,
				neighbourRESTFacade,
				myName,
				backoffProperties,
				discovererProperties);
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

		neighbourDiscoverer.checkForNewNeighbours();

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void testKnownNeighbourIsNotAddedToDatabase(){
		Neighbour ericsson = createNeighbour();
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(ericsson));
		when(neighbourRepository.findByName(any(String.class))).thenReturn(ericsson);

		neighbourDiscoverer.checkForNewNeighbours();

		verify(neighbourRepository, times(0)).save(any(Neighbour.class));
	}

	@Test
	public void testNeighbourWithSameNameAsUsIsNotSaved(){
		// Mocking finding ourselves in the DNS lookup.
		Neighbour discoveringNode = new Neighbour();
		discoveringNode.setName(self.getName());
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(discoveringNode));

		neighbourDiscoverer.checkForNewNeighbours();

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
		Assert.assertTrue(capabilities.getDataTypes().isEmpty());

		doReturn(capabilities).when(neighbourRESTFacade).postCapabilitiesToCapabilities(any(Self.class), any(Neighbour.class));
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));


		doReturn(self).when(selfRepository).findByName(any(String.class));

		neighbourDiscoverer.capabilityExchange(Collections.singletonList(ericsson), self);

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void successfulSubscriptionRequestCallsSaveOnRepository(){
		Neighbour ericsson = createNeighbour();
		doReturn(createFirstSubscriptionRequestResponse()).when(neighbourRESTFacade).postSubscriptionRequest(any(Self.class), any(Neighbour.class),anySet());
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		when(selfRepository.findByName(anyString())).thenReturn(self);

		neighbourDiscoverer.evaluateAndPostSubscriptionRequest(Collections.singletonList(ericsson));

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	private SubscriptionRequest createFirstSubscriptionRequestResponse() {
		Subscription firstSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);
		return new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
	}


	/**
	 * Tests for graceful backoff algorithm
	 */

	@Test
	public void calculatedNextPostAttemptTimeIsInCorrectInterval(){
		Neighbour ericsson = createNeighbour();
		LocalDateTime now = LocalDateTime.now();

		// Mocking the first backoff attempt, where the exponential is 0.
		double exponential = 0;
		long expectedBackoff = (long) Math.pow(2, exponential)*2; //

		System.out.println("LocalDataTime now: "+ now.toString());
		LocalDateTime lowerLimit = now.plusSeconds(expectedBackoff);
		LocalDateTime upperLimit = now.plusSeconds(expectedBackoff+60);

		System.out.println("Lower limit: " + lowerLimit.toString());
		System.out.println("Upper limit: " + upperLimit.toString());

		ericsson.setBackoffAttempts(0);
		ericsson.setBackoffStart(now);

		LocalDateTime result = neighbourDiscoverer.getNextPostAttemptTime(ericsson);

		Assert.assertTrue(result.isAfter(lowerLimit) && result.isBefore(upperLimit));
	}

	@Test
	public void gracefulBackoffPostOfCapabilityDoesNotHappenBeforeAllowedPostTime(){
		Neighbour ericsson = createNeighbour();
		when(neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		ericsson.setBackoffStart(futureTime);

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		verify(neighbourRESTFacade, times(0)).postCapabilitiesToCapabilities(any(Self.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestDoesNotHappenBeforeAllowedTime(){
		Neighbour ericsson = createNeighbour();
		when(neighbourRepository.findByFedIn_StatusIn(SubscriptionRequestStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		ericsson.setBackoffStart(futureTime);
		ericsson.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, getDataTypeSetOriginatingCountry("FI")));
		when(selfRepository.findByName(anyString())).thenReturn(self);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(a -> a.getArgument(0));

		neighbourDiscoverer.gracefulBackoffPostSubscriptionRequest();

		verify(neighbourRESTFacade, times(0)).postSubscriptionRequest(any(Self.class), any(Neighbour.class), any());
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionDoesNotHappenBeforeAllowedTime(){
		Neighbour ericsson = createNeighbour();
		// Neighbour ericsson has subscription to poll
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		// Setting up Ericsson's failed subscriptions
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED);
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(ericssonSubscription));
		ericsson.setFedIn(subReq);
		ericsson.setBackoffStart(LocalDateTime.now().plusSeconds(10));
		neighbourDiscoverer.gracefulBackoffPollSubscriptions();

		verify(neighbourRESTFacade, times(0)).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPostOfCapabilitiesHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();
		when(neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));

		DataType firstDataType = this.datexNo;
		Capabilities ericssonCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(firstDataType));

		doReturn(ericssonCapabilities).when(neighbourRESTFacade).postCapabilitiesToCapabilities(any(Self.class),any(Neighbour.class));

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.setBackoffStart(pastTime);
		doReturn(self).when(selfRepository).findByName(any(String.class));

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		verify(neighbourRESTFacade, times(1)).postCapabilitiesToCapabilities(any(Self.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();
		when(neighbourRepository.findByFedIn_StatusIn(SubscriptionRequestStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		doReturn(createFirstSubscriptionRequestResponse()).when(neighbourRESTFacade).postSubscriptionRequest(any(Self.class), any(Neighbour.class),anySet());
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.setBackoffStart(pastTime);
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		doReturn(self).when(selfRepository).findByName(any(String.class));
		ericsson.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, getDataTypeSetOriginatingCountry("FI")));
		when(selfRepository.findByName(anyString())).thenReturn(self);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(a -> a.getArgument(0));

		neighbourDiscoverer.gracefulBackoffPostSubscriptionRequest();

		verify(neighbourRESTFacade, times(1)).postSubscriptionRequest(any(Self.class), any(Neighbour.class),anySet());
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionHappensIfAllowedPostTimeHasPassed(){
		Neighbour ericsson = createNeighbour();
		// Return an Neighbour with a subscription to poll.
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));

		// Mock result of polling in backoff.
		Subscription ericssonSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED);
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(ericssonSubscription));
		ericsson.setFedIn(subReq);
		doReturn(ericssonSubscription).when(neighbourRESTFacade).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.setBackoffStart(pastTime);
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));

		neighbourDiscoverer.gracefulBackoffPollSubscriptions();

		verify(neighbourRESTFacade, times(1)).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void failedPostOfCapabilitiesInBackoffIncreasesNumberOfBackoffAttempts(){
		Neighbour ericsson = createNeighbour();
		ericsson.setBackoffAttempts(0);
		when(neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(5);

		ericsson.setBackoffStart(pastTime);
		Mockito.doThrow(new CapabilityPostException("Exception from mock")).when(neighbourRESTFacade).postCapabilitiesToCapabilities(any(Self.class), any(Neighbour.class));
		doReturn(self).when(selfRepository).findByName(any(String.class));

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		Assert.assertEquals(1, ericsson.getBackoffAttempts());
	}

	@Test
	public void unsuccessfulPostOfCapabilitiesToNodeWithTooManyPreviousBackoffAttemptsMarksCapabilitiesUnreachable(){
		Neighbour ericsson = createNeighbour();
		ericsson.setBackoffAttempts(4);

		Capabilities ericssonCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.FAILED, Collections.singleton(datexNo));
		ericsson.setCapabilities(ericssonCapabilities);

		when(neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.setBackoffStart(pastTime);
		doThrow(new CapabilityPostException("Exception from mock")).when(neighbourRESTFacade).postCapabilitiesToCapabilities(any(Self.class), any(Neighbour.class));

		doReturn(self).when(selfRepository).findByName(any(String.class));

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		Assert.assertEquals(ericsson.getCapabilities().getStatus(), Capabilities.CapabilitiesStatus.UNREACHABLE);
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
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus.REQUESTED, SubscriptionStatus.ACCEPTED)).thenReturn(Collections.singletonList(ericsson));

		Subscription polledSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED);
		when(neighbourRESTFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(polledSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourDiscoverer.pollSubscriptions();

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void allSubscriptionsHaveFinalStatusFlipsFedInStatusToEstablished(){

		Neighbour spyNeighbour = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour.setFedIn(subscriptionRequest);
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus.REQUESTED, SubscriptionStatus.ACCEPTED)).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED);
		when(neighbourRESTFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourDiscoverer.pollSubscriptions();

		Assert.assertEquals(spyNeighbour.getFedIn().getStatus(), SubscriptionRequestStatus.ESTABLISHED);

	}

	@Test
	public void allSubscriptionsRejectedFlipsFedInStatusToRejected(){
		Neighbour spyNeighbour = spy(Neighbour.class);

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour.setFedIn(subscriptionRequest);
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus.REQUESTED, SubscriptionStatus.ACCEPTED)).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REJECTED);
		when(neighbourRESTFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourDiscoverer.pollSubscriptions();

		Assert.assertEquals(spyNeighbour.getFedIn().getStatus(), SubscriptionRequestStatus.REJECTED);
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

		neighbourDiscoverer.pollSubscriptions();

		Assert.assertEquals(spyNeighbour.getFedIn().getStatus(), SubscriptionRequestStatus.REQUESTED);
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
		when(selfRepository.findByName(anyString())).thenReturn(discoveringNode);

		neighbourDiscoverer.evaluateAndPostSubscriptionRequest(neighbours);

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
	public void twoNeighboursWhereOneHasNoOverlapBeforeAndAfterNewSubscriptionCalculationLetsSecondOneBeCalculated() {
		Self self = new Self("self");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.emptySet());
		Neighbour neighbour = new Neighbour("neighbour",new Capabilities(),subscriptionRequest,new SubscriptionRequest());

		//just to test that I have set up the neighbour and self correctly for the actual test.
		Assert.assertTrue(neighbour.hasEstablishedSubscriptions());
		Assert.assertTrue(self.calculateCustomSubscriptionForNeighbour(neighbour).isEmpty());
		Assert.assertTrue(neighbour.getFedIn().getSubscriptions().isEmpty());
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in

		Neighbour otherNeighbour = new Neighbour("otherNeighbour",
				new Capabilities(),
				new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED,Collections.emptySet()),
				new SubscriptionRequest());

		when(selfRepository.findByName(anyString())).thenReturn(self);

	    neighbourDiscoverer.evaluateAndPostSubscriptionRequest(Arrays.asList(neighbour,otherNeighbour));
	    verify(neighbourRepository,times(2)).save(any(Neighbour.class));
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
		Capabilities neighbourCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,dataTypeSet);
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilities,subscriptionRequest,new SubscriptionRequest());
		Set<Subscription> neighbourFedInSubscription = new HashSet<>();
		neighbourFedInSubscription.add(new Subscription("originatingCountry = 'NO'",SubscriptionStatus.ACCEPTED));
		neighbour.setFedIn(new SubscriptionRequest(null,neighbourFedInSubscription));

		Assert.assertTrue(neighbour.hasEstablishedSubscriptions());
		Set<Subscription> subscriptions = self.calculateCustomSubscriptionForNeighbour(neighbour);
		Assert.assertFalse(subscriptions.isEmpty());
		Assert.assertEquals(neighbour.getFedIn().getSubscriptions(), subscriptions);
		when(neighbourRepository.save(any(Neighbour.class))).thenAnswer(i -> i.getArguments()[0]); // return the argument sent in

		Neighbour otherNeighbour = new Neighbour("otherNeighbour",
				new Capabilities(),
				new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED,Collections.emptySet()),
				new SubscriptionRequest());

		when(selfRepository.findByName(anyString())).thenReturn(self);

		neighbourDiscoverer.evaluateAndPostSubscriptionRequest(Arrays.asList(neighbour,otherNeighbour));
		verify(neighbourRepository).save(otherNeighbour);

    }

}
