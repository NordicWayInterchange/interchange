package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NeighbourDiscovererTest {

	// Mocks
	private InterchangeRepository interchangeRepository = mock(InterchangeRepository.class);
	private ServiceProviderRepository serviceProviderRepository = mock(ServiceProviderRepository.class);
	private DNSFacade dnsFacade = mock(DNSFacade.class);
	private RestTemplate restTemplate = mock(RestTemplate.class);

	private String myName = "bouvet";
	private int backoffIntervalLength = 120;
	private int allowedNumberOfBackoffAttempts = 4;
	private String subscriptionRequestPath = "/requestSubscription";
	private String capabilityExchangePath = "/updateCapabilities";

	@Spy
	private NeighbourDiscoverer neighbourDiscoverer = new NeighbourDiscoverer(dnsFacade,
			interchangeRepository,
			serviceProviderRepository,
			restTemplate,
			myName,
			backoffIntervalLength,
			allowedNumberOfBackoffAttempts,
			subscriptionRequestPath,
			capabilityExchangePath);
	private List<Interchange> neighbours = new ArrayList<>();

	// Objects used in testing
	private Interchange ericsson;
	private DataType firstDataType;
	private DataType secondDataType;
	private DataType thirdDataType;
	private ServiceProvider firstServiceProvider;
	private ServiceProvider secondServiceProvider;
	private ServiceProvider illegalServiceProvider;
	private Iterable<ServiceProvider> serviceProviders;
	private Iterable<ServiceProvider> illegalServiceProviders;
	private Subscription firstSubscription;
	private Subscription secondSubscription;
	private Subscription thirdSubscrption;
	private Subscription illegalSubscription;


	@Before
	public void before(){

		// Neighbour interchange set up
		ericsson = new Interchange();
		ericsson.setName("ericsson");
		ericsson.setDomainName(".itsinterchange.eu");
		ericsson.setControlChannelPort("8080");
		neighbours.add(ericsson);

		firstDataType = new DataType("datex2;1.0", "NO", "Obstruction");
		secondDataType = new DataType("datex2;1.0", "SE", "Works");
		thirdDataType = new DataType("datex2;1.0", "FI", "Conditions");

		firstSubscription = new Subscription("where1 LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		secondSubscription = new Subscription("where1 LIKE 'FI'", Subscription.SubscriptionStatus.REQUESTED);
		thirdSubscrption = new Subscription("where1 LIKE 'DK'", Subscription.SubscriptionStatus.REQUESTED);
		illegalSubscription = new Subscription("(where1 LIKE 'DK') OR (1=1)", Subscription.SubscriptionStatus.REQUESTED);

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
		Assert.assertTrue(neighbourDiscoverer.setContainsDataType(firstDataType, testCapabilities));
	}

	@Test
	public void dataTypeNotInCapabilitiesReturnsFalse(){
		Set<DataType> testCapabilities = Collections.singleton(secondDataType);
		Assert.assertFalse(neighbourDiscoverer.setContainsDataType(firstDataType, testCapabilities));
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
		neighbourInterchange.setCapabilities(Collections.singleton(firstDataType));

		Set<Subscription> calculatedCustomSubscription = neighbourDiscoverer.calculateCustomSubscriptionForNeighbour(neighbourInterchange);

		Assert.assertEquals(1, calculatedCustomSubscription.size());
	}

	@Test
	public void subscriptionRequestForNeighbourWithNoCommonInterestsIsEmpty(){
		when(serviceProviderRepository.findAll()).thenReturn(serviceProviders);
		Interchange neighbourInterchange = new Interchange();
		neighbourInterchange.setName("BMW");
		neighbourInterchange.setCapabilities(Collections.singleton(secondDataType));

		Set<Subscription> calculatedCustomSubscription = neighbourDiscoverer.calculateCustomSubscriptionForNeighbour(neighbourInterchange);

		Assert.assertEquals(0, calculatedCustomSubscription.size());
	}

	@Test
	public void illegalSelectorGivesEmptySubscription(){
		when(serviceProviderRepository.findAll()).thenReturn(illegalServiceProviders);
		Interchange neighbourInterchange = new Interchange();
		neighbourInterchange.setName("BMW");
		neighbourInterchange.setCapabilities(Collections.singleton(secondDataType));

		Set<Subscription> calculatedCustomSubscription = neighbourDiscoverer.calculateCustomSubscriptionForNeighbour(neighbourInterchange);

		Assert.assertTrue(calculatedCustomSubscription.isEmpty());
	}

	@Test
	public void expectedUrlIsCreated(){
		String expectedURL = "http://ericsson.itsinterchange.eu:8080";
		String actualURL = neighbourDiscoverer.getUrl(ericsson);

		Assert.assertEquals(expectedURL, actualURL);
	}

	/**
	 * Tests for method capabilityExchangeWithUpdatedNeighbour()
	 */

	@Test
	public void checkSuccessfulPostToUpdatedNeighbour(){
		when(interchangeRepository.findInterchangesWithRecentCapabilityChanges(any(Timestamp.class), any(Timestamp.class))).thenReturn(Arrays.asList(ericsson));
		doReturn(Collections.singleton(firstSubscription)).when(neighbourDiscoverer).calculateCustomSubscriptionForNeighbour(ericsson);
		doReturn(Collections.singleton(firstSubscription)).when(neighbourDiscoverer).postSubscriptionRequest(any(Interchange.class), any(Interchange.class));

		neighbourDiscoverer.capabilityExchangeWithUpdatedNeighbours();

		verify(neighbourDiscoverer, times(1)).updateSubscriptionsOfNeighbour(any(Interchange.class), anySet());
	}

	@Test
	public void checkUnsuccessfulPostToUpdatedNeighbourSetsStatusOfNeighbour(){

		when(interchangeRepository.findInterchangesWithRecentCapabilityChanges(any(Timestamp.class), any(Timestamp.class))).thenReturn(Arrays.asList(ericsson));
		doReturn(Collections.singleton(firstSubscription)).when(neighbourDiscoverer).calculateCustomSubscriptionForNeighbour(ericsson);
		doThrow(new SubscriptionRequestException("Exception from mock")).when(neighbourDiscoverer).postSubscriptionRequest(any(Interchange.class), any(Interchange.class));

		neighbourDiscoverer.capabilityExchangeWithUpdatedNeighbours();

		verify(neighbourDiscoverer, times(0)).updateSubscriptionsOfNeighbour(any(Interchange.class), anySet());
		verify(interchangeRepository, times(1)).save(any(Interchange.class));

	}

	@Test
	public void noUpdatedNeighboursDoesNotCausePost(){
		when(interchangeRepository.findInterchangesWithRecentCapabilityChanges(any(Timestamp.class), any(Timestamp.class))).thenReturn(Collections.emptyList());

		neighbourDiscoverer.capabilityExchangeWithUpdatedNeighbours();

		verify(neighbourDiscoverer, times(0)).updateSubscriptionsOfNeighbour(any(Interchange.class), anySet());
		verify(interchangeRepository, times(0)).save(any(Interchange.class));
	}


	/**
	 * Tests for graceful backoff algorithm
	 */

	@Test
	public void calculatedNextPostAttemptTimeIsInCorrectInterval(){

		LocalDateTime now = LocalDateTime.now();

		double exponential = 0;
		long expectedBackoff = (long) Math.pow(2, exponential)*backoffIntervalLength;

		LocalDateTime lowerLimit = now.plusSeconds(expectedBackoff);
		LocalDateTime upperLimit = now.plusSeconds(expectedBackoff+60);

		System.out.println("Lower limit: " + lowerLimit.toString());
		System.out.println("Upper limit: " + upperLimit.toString());

		ericsson.setBackoffAttempts(0);
		ericsson.setBackoffStart(now);

		LocalDateTime result = neighbourDiscoverer.getNextPostAttemptTime(ericsson);

		Assert.assertTrue(result.isAfter(lowerLimit) || result.isBefore(upperLimit));
	}

	@Test
	public void gracefulBackoffPostOfCapabilityDoesNotHappenBeforeAllowedPostTime(){
		when(interchangeRepository.findInterchangesWithStatusFAILED_CAPABILITY_EXCHANGE()).thenReturn(Arrays.asList(ericsson));
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10);
		doReturn(futureTime).when(neighbourDiscoverer).getNextPostAttemptTime(ericsson);

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		verify(neighbourDiscoverer, times(0)).postCapabilities(any(Interchange.class), any(Interchange.class));
	}

	@Test
	public void gracefulBackoffPostOfCapabilitiesHappensIfAllowedPostTimeHasPassed(){
		when(interchangeRepository.findInterchangesWithStatusFAILED_CAPABILITY_EXCHANGE()).thenReturn(Arrays.asList(ericsson));
		LocalDateTime pastTime = LocalDateTime.now().minusSeconds(10);
		doReturn(pastTime).when(neighbourDiscoverer).getNextPostAttemptTime(ericsson);
		Interchange neighbourResponse = mock(Interchange.class);
		doReturn(neighbourResponse).when(neighbourDiscoverer).postCapabilities(any(Interchange.class), any(Interchange.class));
		doReturn(Collections.singleton(firstSubscription)).when(neighbourDiscoverer).postSubscriptionRequest(any(Interchange.class), any(Interchange.class));

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		verify(neighbourDiscoverer, times(1)).postCapabilities(any(Interchange.class), any(Interchange.class));
	}

	@Test
	public void failedPostOfCapabilitiesInBackoffIncreasesNumberOfBackoffAttempts(){
		ericsson.setBackoffAttempts(0);
		when(interchangeRepository.findInterchangesWithStatusFAILED_CAPABILITY_EXCHANGE()).thenReturn(Arrays.asList(ericsson));
		LocalDateTime pastTime = LocalDateTime.now().minusSeconds(10);
		doReturn(pastTime).when(neighbourDiscoverer).getNextPostAttemptTime(ericsson);
		doThrow(new CapabilityPostException("Exception from mock")).when(neighbourDiscoverer).postCapabilities(any(Interchange.class), any(Interchange.class));

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		Assert.assertEquals(1, ericsson.getBackoffAttempts());
	}

	@Test
	public void unsuccessfulPostOfCapabilitiesToNodeTooManyPreviousBackoffAttemptsMarksNodeUnreachable(){
		ericsson.setBackoffAttempts(4);
		when(interchangeRepository.findInterchangesWithStatusFAILED_CAPABILITY_EXCHANGE()).thenReturn(Arrays.asList(ericsson));
		LocalDateTime pastTime = LocalDateTime.now().minusSeconds(10);
		doReturn(pastTime).when(neighbourDiscoverer).getNextPostAttemptTime(ericsson);
		doThrow(new CapabilityPostException("Exception from mock")).when(neighbourDiscoverer).postCapabilities(any(Interchange.class), any(Interchange.class));

		neighbourDiscoverer.gracefulBackoffPostCapabilities();

		Assert.assertEquals(ericsson.getInterchangeStatus(), Interchange.InterchangeStatus.UNREACHABLE);
	}

	@Test
	public void postingCapabilitiesToRespondingNeighbourIsSuccessful(){

		// Testing post Capabilities to new interchange

		// Test that an interchange marked as new as discovered in this method.
		// findInterchangesWithCapabilitiesNEW()
		// verify that the list of new neighbours is as log as we expected

		// mock not null Interchange object return value from method postCapabilities() - indicating successful post of capabilities.

		// mock a Set<Subscription> return value from method calculateCustomSubscription() that is not empty

		// verify that the subscription is saved on the neighbour (setFedIn )

		// mocking post response wth Interchange object as body.
		// verify that post subscription request to neighbour is sent



		// second test: Mock that Interchange object return value from postCapabilities is null

	}


	@Test
	public void postCapabilitiesSuccessfulPosts(){
		// Call method postCapabilities and mock a positive post response (method returns true)

		// Call method postCapabilities and mock a negative post response (method returns false)
	}

	@Test
	public void postCapabilitiesThrowsExceptionResponseIsNull(){

		// mock null post response

		// verify exception is thrown

	}

	@Test
	public void postCapabilitiesThrowsExceptionWhenResponseHasWrongName(){
		// mock database lookup with a certain name
		// mock post response with different name

		// verify exception is thrown

	}

	@Test
	public void postCapabilitiesResponseCodeOtherThanCreatedThrowsException(){

		// mock response with different response code
		// verify that exception is thrown

	}





}
