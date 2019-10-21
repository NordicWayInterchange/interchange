package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.DiscoveryStateRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@SuppressWarnings("FieldCanBeLocal")
//@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class NeighbourDiscovererTest {

	// Mocks
	private NeighbourRepository neighbourRepository = mock(NeighbourRepository.class);
	private DNSFacade dnsFacade = mock(DNSFacade.class);
	private NeighbourRESTFacade neighbourRESTFacade = mock(NeighbourRESTFacade.class);
	private NeighbourDiscovererProperties discovererProperties = mock(NeighbourDiscovererProperties.class);
	private GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
	private SelfRepository selfRepository = mock(SelfRepository.class);
	private DiscoveryStateRepository discoveryStateRepository = mock(DiscoveryStateRepository.class);

	private String myName = "bouvet";

	@Spy
	private NeighbourDiscoverer neighbourDiscoverer = new NeighbourDiscoverer(dnsFacade,
			neighbourRepository,
			selfRepository,
			discoveryStateRepository,
			neighbourRESTFacade,
			myName,
			backoffProperties,
			discovererProperties);


	private List<Neighbour> neighbours = new ArrayList<>();

	// Objects used in testing
	private Neighbour ericsson;
	private DataType firstDataType = new DataType("datex2;1.0", "NO", "Obstruction");
	private DataType secondDataType = new DataType("datex2;1.0", "SE", "Works");
	private ServiceProvider firstServiceProvider;
	private ServiceProvider secondServiceProvider;
	private ServiceProvider illegalServiceProvider;
	private Iterable<ServiceProvider> serviceProviders;
	private Iterable<ServiceProvider> illegalServiceProviders;
	private Self self;
	private Subscription firstSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
	private Subscription secondSubscription = new Subscription("where LIKE 'FI'", Subscription.SubscriptionStatus.REQUESTED);
	private Subscription illegalSubscription = new Subscription("(where LIKE 'DK') OR (1=1)", Subscription.SubscriptionStatus.REQUESTED);



	@Before
	public void before(){

		// Self setup
		self = new Self("Bouvet");
		Set<DataType> selfCapabilities = Collections.singleton(new DataType("datex2;1.0", "NO", "Obstruction"));
		self.setLocalCapabilities(selfCapabilities);
		Set<Subscription> selfSubscriptions = Collections.singleton(new Subscription("where LIKE 'FI'", Subscription.SubscriptionStatus.REQUESTED));
		self.setLocalSubscriptions(selfSubscriptions);

		// Neighbour set up
		ericsson = new Neighbour();
		ericsson.setName("ericsson.itsinterchange.eu");
		ericsson.setControlChannelPort("8080");
		neighbours.add(ericsson);

		// Service provider set up: two identical service providers with different names.
		firstServiceProvider = new ServiceProvider("Scania");
		Capabilities firstCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(firstDataType));
		SubscriptionRequest firstSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		firstServiceProvider.setCapabilities(firstCapabilities);
		firstServiceProvider.setSubscriptionRequest(firstSubscriptionRequest);

		secondServiceProvider = new ServiceProvider("Volvo");
		Capabilities secondCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(firstDataType));
		SubscriptionRequest secondSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		secondServiceProvider.setCapabilities(secondCapabilities);
		secondServiceProvider.setSubscriptionRequest(secondSubscriptionRequest);

		illegalServiceProvider = new ServiceProvider("Tesla");
		Capabilities illegalCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(firstDataType));
		SubscriptionRequest illegalSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(illegalSubscription));
		illegalServiceProvider.setCapabilities(illegalCapabilities);
		illegalServiceProvider.setSubscriptionRequest(illegalSubscriptionRequest);

		serviceProviders = Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toSet());
		illegalServiceProviders = Stream.of(illegalServiceProvider).collect(Collectors.toSet());
	}

	@Test
	public void testNewNeighbourIsAddedToDatabase(){
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(ericsson));
		when(neighbourRepository.findByName(any(String.class))).thenReturn(null);
		when(neighbourRepository.save(any(Neighbour.class))).thenReturn(mock(Neighbour.class));

		neighbourDiscoverer.checkForNewNeighbours();

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void testKnownNeighbourIsNotAddedToDatabase(){
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(ericsson));
		when(neighbourRepository.findByName(any(String.class))).thenReturn(ericsson);

		neighbourDiscoverer.checkForNewNeighbours();

		verify(neighbourRepository, times(0)).save(any(Neighbour.class));
	}

	@Test
	public void testNeighbourWithSameNameAsUsIsNotSaved(){
		// Mocking finding ourselves in the DNS lookup.
		doReturn(self).when(selfRepository).findByName(any(String.class));

		Neighbour discoveringNode = neighbourDiscoverer.getDiscoveringNeighbourWithCapabilities();
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(discoveringNode));

		neighbourDiscoverer.checkForNewNeighbours();

		verify(neighbourRepository, times(0)).save(any(Neighbour.class));
	}


	@Test
	public void dataTypeInCapabilitiesReturnsTrue(){
		Set<DataType> testCapabilities = Collections.singleton(firstDataType);
		Assert.assertTrue(firstDataType.isContainedInSet(testCapabilities));
	}

	@Test
	public void dataTypeNotInCapabilitiesReturnsFalse(){
		Set<DataType> testCapabilities = Collections.singleton(secondDataType);
		Assert.assertFalse(firstDataType.isContainedInSet(testCapabilities));
	}

	/**
	 * Capabilities exchange and subscription request
	 */

	@Test
	public void successfulCapabilitiesPostCallsSaveOnRepository() throws Exception{

		Method capabilityExchange = NeighbourDiscoverer.class.getDeclaredMethod("capabilityExchange", List.class);
		capabilityExchange.setAccessible(true);

		doReturn(ericsson).when(neighbourRESTFacade).postCapabilities(any(Neighbour.class), any(Neighbour.class));
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));


		doReturn(self).when(selfRepository).findByName(any(String.class));

		capabilityExchange.invoke(neighbourDiscoverer, Collections.singletonList(ericsson));

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}


	@Test
    @Ignore
	public void successfulSubscriptionRequestCallsSaveOnRepository() throws Exception{

		Method subscriptionRequest = NeighbourDiscoverer.class.getDeclaredMethod("subscriptionRequest", List.class);
		subscriptionRequest.setAccessible(true);

		//doReturn(Collections.singleton(firstSubscription)).when(neighbourDiscoverer).calculateCustomSubscriptionForNeighbour(ericsson);
		SubscriptionRequest subscriptionRequestResponse = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		doReturn(subscriptionRequestResponse).when(neighbourRESTFacade).postSubscriptionRequest(any(Neighbour.class), any(Neighbour.class));
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));

		subscriptionRequest.invoke(neighbourDiscoverer, Collections.singletonList(ericsson));

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}


	/**
	 * Tests for graceful backoff algorithm
	 */

	@Test
	public void calculatedNextPostAttemptTimeIsInCorrectInterval(){

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
		when(neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		ericsson.setBackoffStart(futureTime);

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		verify(neighbourRESTFacade, times(0)).postCapabilities(any(Neighbour.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestDoesNotHappenBeforeAllowedTime(){

		when(neighbourRepository.findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		ericsson.setBackoffStart(futureTime);

		neighbourDiscoverer.gracefulBackoffPostSubscriptionRequest();

		verify(neighbourRESTFacade, times(0)).postCapabilities(any(Neighbour.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionDoesNotHappenBeforeAllowedTime(){
		// Neighbour ericsson has subscription to poll
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(Subscription.SubscriptionStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		// Setting up Ericsson's failed subscriptions
		Subscription ericssonSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.FAILED);
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(ericssonSubscription));
		ericsson.setFedIn(subReq);
		ericsson.setBackoffStart(LocalDateTime.now().plusSeconds(10));
		neighbourDiscoverer.gracefulBackoffPollSubscriptions();

		verify(neighbourRESTFacade, times(0)).pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPostOfCapabilitiesHappensIfAllowedPostTimeHasPassed(){

		when(neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));

		Neighbour ericssonResponse = new Neighbour();
		Capabilities ericssonCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(firstDataType));
		SubscriptionRequest ericssonSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		ericssonResponse.setCapabilities(ericssonCapabilities);
		ericssonResponse.setSubscriptionRequest(ericssonSubscriptionRequest);

		doReturn(ericssonResponse).when(neighbourRESTFacade).postCapabilities(any(Neighbour.class), any(Neighbour.class));

		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.setBackoffStart(pastTime);
		doReturn(self).when(selfRepository).findByName(any(String.class));

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		verify(neighbourRESTFacade, times(1)).postCapabilities(any(Neighbour.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestHappensIfAllowedPostTimeHasPassed(){

		when(neighbourRepository.findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		SubscriptionRequest ericssonSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		doReturn(ericssonSubscriptionRequest).when(neighbourRESTFacade).postSubscriptionRequest(any(Neighbour.class), any(Neighbour.class));
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.setBackoffStart(pastTime);
		doReturn(ericsson).when(neighbourRepository).save(any(Neighbour.class));
		doReturn(self).when(selfRepository).findByName(any(String.class));

		neighbourDiscoverer.gracefulBackoffPostSubscriptionRequest();

		verify(neighbourRESTFacade, times(1)).postSubscriptionRequest(any(Neighbour.class), any(Neighbour.class));
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionHappensIfAllowedPostTimeHasPassed(){

		// Return an Neighbour with a subscription to poll.
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(Subscription.SubscriptionStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));

		// Mock result of polling in backoff.
		Subscription ericssonSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.FAILED);
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(ericssonSubscription));
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
		ericsson.setBackoffAttempts(0);
		when(neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(1);

		ericsson.setBackoffStart(pastTime);
		Mockito.doThrow(new CapabilityPostException("Exception from mock")).when(neighbourRESTFacade).postCapabilities(any(Neighbour.class), any(Neighbour.class));
		doReturn(self).when(selfRepository).findByName(any(String.class));

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		Assert.assertEquals(1, ericsson.getBackoffAttempts());
	}

	@Test
	public void unsuccessfulPostOfCapabilitiesToNodeWithTooManyPreviousBackoffAttemptsMarksCapabilitiesUnreachable(){
		ericsson.setBackoffAttempts(4);

		Capabilities ericssonCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.FAILED, Collections.singleton(firstDataType));
		ericsson.setCapabilities(ericssonCapabilities);

		when(neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED)).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		ericsson.setBackoffStart(pastTime);
		doThrow(new CapabilityPostException("Exception from mock")).when(neighbourRESTFacade).postCapabilities(any(Neighbour.class), any(Neighbour.class));

		doReturn(self).when(selfRepository).findByName(any(String.class));

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		Assert.assertEquals(ericsson.getCapabilities().getStatus(), Capabilities.CapabilitiesStatus.UNREACHABLE);
	}

	/*
		Subscription polling tests
	 */

	@Test
	public void successfulPollOfSubscriptionCallsSaveOnRepository(){

		Subscription subscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		SubscriptionRequest ericssonSubscription = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		ericsson.setFedIn(ericssonSubscription);
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(Subscription.SubscriptionStatus.REQUESTED, Subscription.SubscriptionStatus.ACCEPTED)).thenReturn(Collections.singletonList(ericsson));

		Subscription polledSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.ACCEPTED);
		when(neighbourRESTFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(polledSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourDiscoverer.pollSubscriptions();

		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	public void allSubscriptionsHaveFinalStatusFlipsFedInStatusToEstablished(){

		Neighbour spyNeighbour = spy(Neighbour.class);

		Subscription subscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour.setFedIn(subscriptionRequest);
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(Subscription.SubscriptionStatus.REQUESTED, Subscription.SubscriptionStatus.ACCEPTED)).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.CREATED);
		when(neighbourRESTFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourDiscoverer.pollSubscriptions();

		Assert.assertEquals(spyNeighbour.getFedIn().getStatus(), SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);

	}

	@Test
	public void allSubscriptionsRejectedFlipsFedInStatusToRejected(){
		Neighbour spyNeighbour = spy(Neighbour.class);

		Subscription subscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour.setFedIn(subscriptionRequest);
		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(Subscription.SubscriptionStatus.REQUESTED, Subscription.SubscriptionStatus.ACCEPTED)).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REJECTED);
		when(neighbourRESTFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourDiscoverer.pollSubscriptions();

		Assert.assertEquals(spyNeighbour.getFedIn().getStatus(), SubscriptionRequest.SubscriptionRequestStatus.REJECTED);
	}

	@Test
	public void subscriptionStatusAcceptedKeepsFedInStatusRequested(){
		Neighbour spyNeighbour = spy(Neighbour.class);

		Subscription subscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		subscription.setNumberOfPolls(0);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		spyNeighbour.setFedIn(subscriptionRequest);

		when(neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(Subscription.SubscriptionStatus.REQUESTED, Subscription.SubscriptionStatus.ACCEPTED)).thenReturn(Collections.singletonList(spyNeighbour));

		Subscription createdSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.ACCEPTED);
		when(neighbourRESTFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenReturn(createdSubscription);
		when(discovererProperties.getSubscriptionPollingNumberOfAttempts()).thenReturn(7);

		neighbourDiscoverer.pollSubscriptions();

		Assert.assertEquals(spyNeighbour.getFedIn().getStatus(), SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
	}


}
