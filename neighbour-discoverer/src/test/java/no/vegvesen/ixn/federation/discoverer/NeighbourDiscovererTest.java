package no.vegvesen.ixn.federation.discoverer;

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
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
	private NeighbourDiscoverer neighbourDiscoverer = new NeighbourDiscoverer(dnsFacade, interchangeRepository, serviceProviderRepository, restTemplate, "bouvet");
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
		Interchange discoveringNode = neighbourDiscoverer.getRepresentationOfDiscoveringInterchange();
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



}
