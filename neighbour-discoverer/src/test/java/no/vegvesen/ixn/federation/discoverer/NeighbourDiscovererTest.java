package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.mockito.Mockito.*;

@SuppressWarnings("FieldCanBeLocal")
@RunWith(MockitoJUnitRunner.class)
public class NeighbourDiscovererTest {

	// Mocks
	private InterchangeRepository interchangeRepository = mock(InterchangeRepository.class);
	private ServiceProviderRepository serviceProviderRepository = mock(ServiceProviderRepository.class);
	private DNSFacade dnsFacade = mock(DNSFacade.class);
	private NeighbourRESTFacade neighbourRESTFacade = mock(NeighbourRESTFacade.class);
	private NeighbourDiscovererProperties discovererProperties = mock(NeighbourDiscovererProperties.class);
	private GracefulBackoffProperties backoffProperties = mock(GracefulBackoffProperties.class);

	private String myName = "bouvet";

	@Spy
	private NeighbourDiscoverer neighbourDiscoverer = new NeighbourDiscoverer(dnsFacade,
			interchangeRepository,
			serviceProviderRepository,
			neighbourRESTFacade,
			myName,
			backoffProperties,
			discovererProperties);


	private List<Interchange> neighbours = new ArrayList<>();

	// Objects used in testing
	private Interchange ericsson;
	private DataType firstDataType = new DataType("datex2;1.0", "NO", "Obstruction");
	private DataType secondDataType = new DataType("datex2;1.0", "SE", "Works");
	private ServiceProvider firstServiceProvider;
	private ServiceProvider secondServiceProvider;
	private ServiceProvider illegalServiceProvider;
	private Iterable<ServiceProvider> serviceProviders;
	private Iterable<ServiceProvider> illegalServiceProviders;
	private Subscription firstSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
	private Subscription secondSubscription = new Subscription("where LIKE 'FI'", Subscription.SubscriptionStatus.REQUESTED);
	private Subscription illegalSubscription = new Subscription("(where LIKE 'DK') OR (1=1)", Subscription.SubscriptionStatus.REQUESTED);



	@Before
	public void before(){

		// Neighbour interchange set up
		ericsson = new Interchange();
		ericsson.setName("ericsson.itsinterchange.eu");
		ericsson.setControlChannelPort("8080");
		neighbours.add(ericsson);

		// Service provider set up: two identical service providers with different names.
		firstServiceProvider = new ServiceProvider("Scania");
		firstServiceProvider.setCapabilities(Collections.singleton(firstDataType));
		firstServiceProvider.setSubscriptions(Collections.singleton(firstSubscription));

		secondServiceProvider = new ServiceProvider("Volvo");
		secondServiceProvider.setCapabilities(Collections.singleton(firstDataType));
		secondServiceProvider.setSubscriptions(Collections.singleton(firstSubscription));

		illegalServiceProvider = new ServiceProvider("Tesla");
		illegalServiceProvider.setCapabilities(Collections.singleton(firstDataType));
		illegalServiceProvider.setSubscriptions(Collections.singleton(illegalSubscription));

		serviceProviders = Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toSet());
		illegalServiceProviders = Stream.of(illegalServiceProvider).collect(Collectors.toSet());
	}

	@Test
	public void testNewInterchangeIsAddedToDatabase(){
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(ericsson));
		when(interchangeRepository.findByName(any(String.class))).thenReturn(null);
		when(interchangeRepository.save(any(Interchange.class))).thenReturn(mock(Interchange.class));

		neighbourDiscoverer.checkForNewInterchanges();

		verify(interchangeRepository, times(1)).save(any(Interchange.class));
	}

	@Test
	public void testKnownInterchangeIsNotAddedToDatabase(){
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(ericsson));
		when(interchangeRepository.findByName(any(String.class))).thenReturn(ericsson);

		neighbourDiscoverer.checkForNewInterchanges();

		verify(interchangeRepository, times(0)).save(any(Interchange.class));
	}

	@Test
	public void testInterchangeWithSameNameAsUsIsNotSaved(){
		// Mocking finding ourselves in the DNS lookup.
		Interchange discoveringNode = neighbourDiscoverer.getDiscoveringInterchangeWithCapabilities();
		when(dnsFacade.getNeighbours()).thenReturn(Collections.singletonList(discoveringNode));

		neighbourDiscoverer.checkForNewInterchanges();

		verify(interchangeRepository, times(0)).save(any(Interchange.class));
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

	@Test
	public void subscriptionInSubscriptionSetReturnsTrue(){
		Set<Subscription> testSubscriptionsSet = Collections.singleton(firstSubscription);
		Assert.assertTrue(neighbourDiscoverer.setContainsSubscription(firstSubscription, testSubscriptionsSet));
	}

	@Test
	public void subscriptionNotInSubscriptionSetReturnsFalse(){
		Set<Subscription> testSubscriptionSet = Collections.singleton(secondSubscription);

		Assert.assertFalse(neighbourDiscoverer.setContainsSubscription(firstSubscription, testSubscriptionSet));
	}

	@Test
	public void checkThatCapabilitiesDoesNotContainDuplicates(){
		when(serviceProviderRepository.findAll()).thenReturn(serviceProviders);

		Set<DataType> interchangeCapabilities = neighbourDiscoverer.getLocalServiceProviderCapabilities();

		Assert.assertEquals(1, interchangeCapabilities.size());
	}

	@Test
	public void checkThatSubscriptionsDoesNotContainDuplicates(){
		when(serviceProviderRepository.findAll()).thenReturn(serviceProviders);

		Set<Subscription> interchangeSubscriptions = neighbourDiscoverer.getLocalServiceProviderSubscriptions();

		Assert.assertEquals(1, interchangeSubscriptions.size());
	}

	@Test
	public void subscriptionRequestForNeighbourWithCommonInterestIsCalculatedCorrectly(){
		when(serviceProviderRepository.findAll()).thenReturn(serviceProviders);
		Interchange neighbourInterchange = new Interchange();
		neighbourInterchange.setName("BMW");
		neighbourInterchange.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,Collections.singleton(firstDataType)));

		Set<Subscription> calculatedCustomSubscription = neighbourDiscoverer.calculateCustomSubscriptionForNeighbour(neighbourInterchange);

		Assert.assertEquals(1, calculatedCustomSubscription.size());
	}

	@Test
	public void subscriptionRequestForNeighbourWithNoCommonInterestsIsEmpty(){
		when(serviceProviderRepository.findAll()).thenReturn(serviceProviders);
		Interchange neighbourInterchange = new Interchange();
		neighbourInterchange.setName("BMW");
		neighbourInterchange.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(secondDataType)));

		Set<Subscription> calculatedCustomSubscription = neighbourDiscoverer.calculateCustomSubscriptionForNeighbour(neighbourInterchange);

		Assert.assertEquals(0, calculatedCustomSubscription.size());
	}

	@Test
	public void illegalSelectorGivesEmptySubscription(){
		when(serviceProviderRepository.findAll()).thenReturn(illegalServiceProviders);
		Interchange neighbourInterchange = new Interchange();
		neighbourInterchange.setName("BMW");
		neighbourInterchange.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(secondDataType)));

		Set<Subscription> calculatedCustomSubscription = neighbourDiscoverer.calculateCustomSubscriptionForNeighbour(neighbourInterchange);

		Assert.assertTrue(calculatedCustomSubscription.isEmpty());
	}


	/**
	 * Capabilities exchange and subscription request
	 */

	@Test
	public void successfulCapabilitiesPostCallsSaveOnRepository(){

		doReturn(Collections.singletonList(ericsson)).when(interchangeRepository).findInterchangesForCapabilityExchange();
		doReturn(ericsson).when(neighbourRESTFacade).postCapabilities(any(Interchange.class), any(Interchange.class));
		doReturn(ericsson).when(interchangeRepository).save(any(Interchange.class));

		neighbourDiscoverer.capabilityExchange();

		verify(interchangeRepository, times(1)).save(any(Interchange.class));
	}


	@Test
	public void successfulSubscriptionRequestCallsSaveOnRepository(){

		doReturn(Collections.singletonList(ericsson)).when(interchangeRepository).findInterchangesForSubscriptionRequest();
		doReturn(Collections.singleton(firstSubscription)).when(neighbourDiscoverer).calculateCustomSubscriptionForNeighbour(ericsson);
		SubscriptionRequest subscriptionRequestResponse = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		doReturn(subscriptionRequestResponse).when(neighbourRESTFacade).postSubscriptionRequest(any(Interchange.class), any(Interchange.class));
		doReturn(ericsson).when(interchangeRepository).save(any(Interchange.class));

		neighbourDiscoverer.subscriptionRequest();

		verify(interchangeRepository, times(1)).save(any(Interchange.class));
	}


	@Test
	public void noNeighboursFoundForCapabilityExchangeCausesNoPost(){
		when(interchangeRepository.findInterchangesForCapabilityExchange()).thenReturn(Collections.emptyList());

		neighbourDiscoverer.capabilityExchange();

		verify(neighbourRESTFacade, times(0)).postCapabilities(any(Interchange.class), any(Interchange.class));
	}


	/**
	 * Tests for graceful backoff algorithm
	 */

	@Test
	public void calculatedNextPostAttemptTimeIsInCorrectInterval(){

		LocalDateTime now = LocalDateTime.now();

		double exponential = 0;
		long expectedBackoff = (long) Math.pow(2, exponential)*2000;

		LocalDateTime lowerLimit = now.plusSeconds(expectedBackoff);
		LocalDateTime upperLimit = now.plusSeconds(expectedBackoff+60);

		System.out.println("Lower limit: " + lowerLimit.toString());
		System.out.println("Upper limit: " + upperLimit.toString());

		ericsson.setBackoffAttempts(0);
		ericsson.setBackoffStart(now);

		doReturn(2000).when(backoffProperties).getStartIntervalLength();
		doReturn(60000).when(backoffProperties).getRandomShiftUpperLimit();


		LocalDateTime result = neighbourDiscoverer.getNextPostAttemptTime(ericsson);

		Assert.assertTrue(result.isAfter(lowerLimit) || result.isBefore(upperLimit));
	}

	@Test
	public void gracefulBackoffPostOfCapabilityDoesNotHappenBeforeAllowedPostTime(){
		when(interchangeRepository.findInterchangesWithFailedCapabilityExchange()).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		doReturn(futureTime).when(neighbourDiscoverer).getNextPostAttemptTime(ericsson);

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		verify(neighbourRESTFacade, times(0)).postCapabilities(any(Interchange.class), any(Interchange.class));
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestDoesNotHappenBeforeAllowedTime(){

		when(interchangeRepository.findInterchangesWithFailedFedIn()).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		doReturn(futureTime).when(neighbourDiscoverer).getNextPostAttemptTime(ericsson);

		neighbourDiscoverer.gracefulBackoffPostSubscriptionRequest();

		verify(neighbourRESTFacade, times(0)).postCapabilities(any(Interchange.class), any(Interchange.class));
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionDoesNotHappenBeforeAllowedTime(){
		// Interchange ericsson has subscription to poll
		when(interchangeRepository.findInterchangesWithFailedSubscriptionsInFedIn()).thenReturn(Collections.singletonList(ericsson));
		// Setting up Ericsson's failed subscriptions
		Subscription ericssonSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.FAILED);
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(ericssonSubscription));
		ericsson.setFedIn(subReq);
		doReturn(LocalDateTime.now().plusSeconds(10)).when(neighbourDiscoverer).getNextPostAttemptTime(any(Interchange.class));

		neighbourDiscoverer.gracefulBackoffPollSubscriptions();

		verify(neighbourRESTFacade, times(0)).pollSubscriptionStatus(any(Subscription.class), any(Interchange.class));
	}

	@Test
	public void gracefulBackoffPostOfCapabilitiesHappensIfAllowedPostTimeHasPassed(){

		when(interchangeRepository.findInterchangesWithFailedCapabilityExchange()).thenReturn(Collections.singletonList(ericsson));

		Interchange ericssonResponse = new Interchange();
		Capabilities ericssonCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(firstDataType));
		SubscriptionRequest ericssonSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		ericssonResponse.setCapabilities(ericssonCapabilities);
		ericssonResponse.setSubscriptionRequest(ericssonSubscriptionRequest);

		doReturn(ericssonResponse).when(neighbourRESTFacade).postCapabilities(any(Interchange.class), any(Interchange.class));

		LocalDateTime pastTime = LocalDateTime.now().minusSeconds(10);
		doReturn(pastTime).when(neighbourDiscoverer).getNextPostAttemptTime(ericsson);

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		verify(neighbourRESTFacade, times(1)).postCapabilities(any(Interchange.class), any(Interchange.class));
	}

	@Test
	public void gracefulBackoffPostOfSubscriptionRequestHappensIfAllowedPostTimeHasPassed(){

		when(interchangeRepository.findInterchangesWithFailedFedIn()).thenReturn(Collections.singletonList(ericsson));
		SubscriptionRequest ericssonSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		doReturn(ericssonSubscriptionRequest).when(neighbourRESTFacade).postSubscriptionRequest(any(Interchange.class), any(Interchange.class));
		LocalDateTime pastTime = LocalDateTime.now().minusSeconds(10);
		doReturn(pastTime).when(neighbourDiscoverer).getNextPostAttemptTime(ericsson);
		doReturn(ericsson).when(interchangeRepository).save(any(Interchange.class));

		neighbourDiscoverer.gracefulBackoffPostSubscriptionRequest();

		verify(neighbourRESTFacade, times(1)).postSubscriptionRequest(any(Interchange.class), any(Interchange.class));
	}

	@Test
	public void gracefulBackoffPollOfSubscriptionHappensIfAllowedPostTimeHasPassed(){

		// Return an interchange with a subscription to poll.
		when(interchangeRepository.findInterchangesWithFailedSubscriptionsInFedIn()).thenReturn(Collections.singletonList(ericsson));

		// Mock result of polling in backoff.
		Subscription ericssonSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.FAILED);
		SubscriptionRequest subReq = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(ericssonSubscription));
		ericsson.setFedIn(subReq);
		doReturn(ericssonSubscription).when(neighbourRESTFacade).pollSubscriptionStatus(any(Subscription.class), any(Interchange.class));

		LocalDateTime pastTime = LocalDateTime.now().minusSeconds(10);
		doReturn(pastTime).when(neighbourDiscoverer).getNextPostAttemptTime(ericsson);
		doReturn(ericsson).when(interchangeRepository).save(any(Interchange.class));

		neighbourDiscoverer.gracefulBackoffPollSubscriptions();

		verify(neighbourRESTFacade, times(1)).pollSubscriptionStatus(any(Subscription.class), any(Interchange.class));
	}

	@Test
	public void failedPostOfCapabilitiesInBackoffIncreasesNumberOfBackoffAttempts(){
		ericsson.setBackoffAttempts(0);
		when(interchangeRepository.findInterchangesWithFailedCapabilityExchange()).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime pastTime = LocalDateTime.now().minusSeconds(10);

		doReturn(pastTime).when(neighbourDiscoverer).getNextPostAttemptTime(ericsson);
		Mockito.doThrow(new CapabilityPostException("Exception from mock")).when(neighbourRESTFacade).postCapabilities(any(Interchange.class), any(Interchange.class));

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		Assert.assertEquals(1, ericsson.getBackoffAttempts());
	}

	@Test
	public void unsuccessfulPostOfCapabilitiesToNodeTooManyPreviousBackoffAttemptsMarksCapabilitiesUnreachable(){
		ericsson.setBackoffAttempts(4);

		Capabilities ericssonCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.FAILED, Collections.singleton(firstDataType));
		ericsson.setCapabilities(ericssonCapabilities);

		when(interchangeRepository.findInterchangesWithFailedCapabilityExchange()).thenReturn(Collections.singletonList(ericsson));
		LocalDateTime pastTime = LocalDateTime.now().minusSeconds(10);
		doReturn(pastTime).when(neighbourDiscoverer).getNextPostAttemptTime(ericsson);
		doThrow(new CapabilityPostException("Exception from mock")).when(neighbourRESTFacade).postCapabilities(any(Interchange.class), any(Interchange.class));

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		Assert.assertEquals(ericsson.getCapabilities().getStatus(), Capabilities.CapabilitiesStatus.UNREACHABLE);
	}


	@Test
	public void successfulPollOfSubscriptionCallsSaveOnRepository(){

		Subscription subscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		SubscriptionRequest ericssonSubscription = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		ericsson.setFedIn(ericssonSubscription);
		when(interchangeRepository.findInterchangesWithSubscriptionToPoll()).thenReturn(Collections.singletonList(ericsson));

		neighbourDiscoverer.pollSubscriptions();

		verify(interchangeRepository, times(1)).save(any(Interchange.class));
	}


}
