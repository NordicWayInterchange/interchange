package no.vegvesen.ixn.federation.service;


import no.vegvesen.ixn.federation.api.v1_0.CapabilitiesApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.DatexCapability;
import no.vegvesen.ixn.federation.model.DenmCapability;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import javax.net.ssl.SSLContext;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class NeighbourDiscovererIT {

	private final LocalDateTime lastUpdatedLocalSubscriptions = LocalDateTime.now();
	@MockBean
	SSLContext mockedSSL;

	@MockBean
	DNSFacade mockDnsFacade;

	@MockBean
	NeighbourFacade mockNeighbourFacade;

	@Autowired
	NeighbourService neighbourService;

	@Autowired
	NeigbourDiscoveryService neighbourDiscoveryService;

	@Autowired
	NeighbourRepository repository;

	@Autowired
	InterchangeNodeProperties nodeProperties;
	private Set<LocalSubscription> localSubscriptions;

	@Test
	public void discovererIsAutowired() {
		assertThat(neighbourService).isNotNull();
	}

	@Test
	public void messageCollectorWillStartAfterCompleteOptimisticControlChannelFlow() {
		assertThat(repository.findAll()).withFailMessage("The test shall start with no neighbours stored. Use @Transactional.").hasSize(0);
		localSubscriptions = new HashSet<>();
		localSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and originatingCountry = 'NO'"));

		Neighbour neighbour1 = new Neighbour();
		neighbour1.setName("neighbour-one");
		Neighbour neighbour2 = new Neighbour();
		neighbour2.setName("neighbour-two");

		Capabilities c1 = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDatexCapability("NO")));
		Capabilities c2 = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDatexCapability("FI")));
		when(mockDnsFacade.lookupNeighbours()).thenReturn(Lists.list(neighbour1, neighbour2));

		checkForNewNeighbours();
		performCapabilityExchangeAndVerifyNeighbourRestFacadeCalls(neighbour1, neighbour2, c1, c2);
		String selector = String.format("messageType = %s and originatingCountry = 'NO'", Constants.DATEX_2);
		HashSet<Subscription> subscriptions = new HashSet<>(Arrays.asList(
				new Subscription(selector, SubscriptionStatus.ACCEPTED, "self"),
				new Subscription(selector, SubscriptionStatus.ACCEPTED, "self")
		));
		when(mockNeighbourFacade.postSubscriptionRequest(any(), anySet(), any())).thenReturn(subscriptions);

		neighbourDiscoveryService.evaluateAndPostSubscriptionRequest(Lists.newArrayList(neighbour1, neighbour2), Optional.ofNullable(lastUpdatedLocalSubscriptions), localSubscriptions, mockNeighbourFacade);

		verify(mockNeighbourFacade, times(1)).postSubscriptionRequest(eq(neighbour1), any(), any());
		verify(mockNeighbourFacade, times(0)).postSubscriptionRequest(eq(neighbour2), any(), any());

		Neighbour found1 = repository.findByName(neighbour1.getName());
		assertThat(found1).isNotNull();
		assertThat(found1.getNeighbourRequestedSubscriptions()).isNotNull();
		assertThat(found1.getSubscriptionsForPolling()).hasSize(1);
		Subscription requestedSubscription = found1.getSubscriptionsForPolling().iterator().next();
		performSubscriptionPolling(neighbour1, requestedSubscription);

		List<Neighbour> toConsumeMessagesFrom = neighbourService.listNeighboursToConsumeMessagesFrom();
		assertThat(toConsumeMessagesFrom).hasSize(1);
		assertThat(toConsumeMessagesFrom).contains(neighbour1);
	}

	private Capability getDatexCapability(String originatingCountry) {
		return new DatexCapability(null, originatingCountry, null, null, null);
	}

	@Test
	public void messageCollectorWillStartAfterCompleteOptimisticControlChannelFlowAndExtraCapabilityExchange() {
		messageCollectorWillStartAfterCompleteOptimisticControlChannelFlow();


		neighbourDiscoveryService.capabilityExchangeWithNeighbours(mockNeighbourFacade, Collections.emptySet(), Optional.of(LocalDateTime.now()));
		verify(mockNeighbourFacade, times(4)).postCapabilitiesToCapabilities(any(), any(), any());

		List<Neighbour> toConsumeMessagesFrom = neighbourService.listNeighboursToConsumeMessagesFrom();
		assertThat(toConsumeMessagesFrom).hasSize(1);
	}

	@Test
	public void messageCollectorWillStartAfterCompleteOptimisticControlChannelFlowAndExtraIncomingCapabilityExchange() {
		messageCollectorWillStartAfterCompleteOptimisticControlChannelFlow();

		neighbourService.incomingCapabilities(new CapabilitiesApi("neighbour-one", Sets.newLinkedHashSet(new DatexCapabilityApi("NO"))), Collections.emptySet());
		List<Neighbour> toConsumeMessagesFrom = neighbourService.listNeighboursToConsumeMessagesFrom();
		assertThat(toConsumeMessagesFrom).hasSize(1);
	}

	@Test
	public void postEmptySubscriptionRequest() {
		neighbourDiscoveryService.postSubscriptionRequest(new Neighbour(),Collections.emptySet(),mockNeighbourFacade);
	}

	@Test
	public void postSubscriptionRequestLocalSubscriptionsMatchesNeighbourState() {
		Set<LocalSubscription> localSubscriptions = Collections.singleton(
				new LocalSubscription(
						LocalSubscriptionStatus.CREATED,
						"originatingCountry = 'NO'",
						"bouvet.itsinterchange.eu"
				)
		);
		Neighbour neighbour = new Neighbour(
				"neighour",
				new Capabilities(
						Capabilities.CapabilitiesStatus.KNOWN,
						Collections.singleton(
								new DatexCapability(
										"NO0001",
										"NO",
										"1.0",
										Collections.emptySet(),
										Collections.emptySet()
								)
						),
						LocalDateTime.now()
				),
				new SubscriptionRequest(),
				new SubscriptionRequest(
						SubscriptionRequestStatus.ESTABLISHED,
						Collections.singleton(
								new Subscription(
										"originatingCountry = 'NO'",
										"bouvet.itsinterchange.eu",
										SubscriptionStatus.CREATED

								)
						)
				)
		);
		repository.save(neighbour);
		neighbourDiscoveryService.postSubscriptionRequest(neighbour, localSubscriptions,mockNeighbourFacade);
		verify(mockNeighbourFacade,never()).postSubscriptionRequest(any(Neighbour.class),anySet(),anyString());
	}

	@Test
	public void oneLocalSubscriptionMatchingOneCapability() {
		Set<LocalSubscription> localSubscriptions = Collections.singleton(
				new LocalSubscription(
						LocalSubscriptionStatus.CREATED,
						"originatingCountry = 'NO'",
						nodeProperties.getName()
				)
		);
		Neighbour neighbour = new Neighbour(
				"neighour",
				new Capabilities(
						Capabilities.CapabilitiesStatus.KNOWN,
						Collections.singleton(
								new DatexCapability(
										"NO0001",
										"NO",
										"1.0",
										Collections.emptySet(),
										Collections.emptySet()
								)
						),
						LocalDateTime.now()
				),
				new SubscriptionRequest(),
				new SubscriptionRequest()
		);
		repository.save(neighbour);
		when(mockNeighbourFacade.postSubscriptionRequest(any(Neighbour.class),anySet(),anyString()))
				.thenReturn(Collections.singleton(
						new Subscription(
								"originatingCountry = 'NO'",
								SubscriptionStatus.REQUESTED,
								nodeProperties.getName()
						)
				)

		);
		neighbourDiscoveryService.postSubscriptionRequest(neighbour,localSubscriptions,mockNeighbourFacade);
		verify(mockNeighbourFacade,times(1)).postSubscriptionRequest(any(Neighbour.class),anySet(),anyString());
	}


	@Test
	public void oneLocalSubscriptionsBothMatchingTwoCapabilities() {
		Set<LocalSubscription> localSubscriptions = new HashSet<>(Arrays.asList(
				new LocalSubscription(
						LocalSubscriptionStatus.CREATED,
						"originatingCountry = 'NO'"
				)
		));
		Neighbour neighbour = new Neighbour(
				"neighour",
				new Capabilities(
						Capabilities.CapabilitiesStatus.KNOWN,
						new HashSet<>(Arrays.asList(
								new DatexCapability(
										"NO0001",
										"NO",
										"1.0",
										Collections.emptySet(),
										Collections.emptySet()
								),
								new DenmCapability(
										"NO0001",
										"NO",
										"1.0",
										Collections.emptySet(),
										Collections.emptySet()
								)
						)),
						LocalDateTime.now()
				),
				new SubscriptionRequest(),
				new SubscriptionRequest()
		);
		repository.save(neighbour);
		//NOTE this will only return one subscription as per SubscriptionCalculator.calculateCustomSubscriptionForNeighbour
		when(mockNeighbourFacade.postSubscriptionRequest(any(Neighbour.class),anySet(),anyString()))
				.thenReturn(new HashSet<>(Arrays.asList(
								new Subscription(
										"originatingCountry = 'NO'",
										SubscriptionStatus.REQUESTED,
										"bouvet.itsinterchange.eu"

								)
						))
				);
		neighbourDiscoveryService.postSubscriptionRequest(neighbour,localSubscriptions,mockNeighbourFacade);
		verify(mockNeighbourFacade,times(1)).postSubscriptionRequest(any(Neighbour.class),anySet(),anyString());
		List<Neighbour> neighbours = repository.findAll();
		assertThat(neighbours).hasSize(1);
		//NOTE this will only return one subscription as per SubscriptionCalculator.calculateCustomSubscriptionForNeighbour
		assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);

	}


	@Test
	public void evaluateAndPostSubscriptionRequestOneNeighbour() {
		Set<LocalSubscription> localSubscriptions = Collections.singleton(
				new LocalSubscription(
						LocalSubscriptionStatus.CREATED,
						"originatingCountry = 'NO'",
						nodeProperties.getName()
				)
		);
		Neighbour neighbour = new Neighbour(
				"neighour",
				new Capabilities(
						Capabilities.CapabilitiesStatus.KNOWN,
						Collections.singleton(
								new DatexCapability(
										"NO0001",
										"NO",
										"1.0",
										Collections.emptySet(),
										Collections.emptySet()
								)
						),
						LocalDateTime.now().minusHours(1)
				),
				new SubscriptionRequest(),
				new SubscriptionRequest()
		);
		neighbour.getCapabilities().setLastCapabilityExchange(LocalDateTime.now());
		repository.save(neighbour);
		//TODO should return a subscription from the post.
		when(mockNeighbourFacade.postSubscriptionRequest(any(Neighbour.class),anySet(),anyString()))
				.thenReturn(Collections.singleton(
						new Subscription(
								"originatingCountry = 'NO'",
								SubscriptionStatus.REQUESTED,
								nodeProperties.getName()

						)
				));
		Optional<LocalDateTime> lastUpdatedTime = Optional.of(LocalDateTime.now());
		assertThat(neighbour.shouldCheckSubscriptionRequestsForUpdates(lastUpdatedTime)).isTrue();
		neighbourDiscoveryService.evaluateAndPostSubscriptionRequest(
				Collections.singletonList(neighbour),
				lastUpdatedTime,
				localSubscriptions,
				mockNeighbourFacade);
		verify(mockNeighbourFacade,times(1)).postSubscriptionRequest(any(Neighbour.class),anySet(),anyString());
		assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);

	}

	@Test
	public void evaluateAndPostSubscriptionRequestTwoNeighbours() {
		Set<LocalSubscription> localSubscriptions = Collections.singleton(
				new LocalSubscription(
						LocalSubscriptionStatus.CREATED,
						"originatingCountry = 'NO'"
				)
		);
		Neighbour neighbourA = new Neighbour(
				"neighourA",
				new Capabilities(
						Capabilities.CapabilitiesStatus.KNOWN,
						Collections.singleton(
								new DatexCapability(
										"NO0001",
										"NO",
										"1.0",
										Collections.emptySet(),
										Collections.emptySet()
								)
						),
						LocalDateTime.now().minusHours(1)
				),
				new SubscriptionRequest(),
				new SubscriptionRequest()
		);
		neighbourA.getCapabilities().setLastCapabilityExchange(LocalDateTime.now());
		Neighbour neighbourB = new Neighbour(
				"neighourB",
				new Capabilities(
						Capabilities.CapabilitiesStatus.KNOWN,
						Collections.singleton(
								new DatexCapability(
										"NO0002",
										"NO",
										"1.0",
										Collections.emptySet(),
										Collections.emptySet()
								)
						),
						LocalDateTime.now().minusHours(1)
				),
				new SubscriptionRequest(),
				new SubscriptionRequest()
		);
		neighbourB.getCapabilities().setLastCapabilityExchange(LocalDateTime.now());
		repository.save(neighbourA);
		repository.save(neighbourB);
		neighbourDiscoveryService.evaluateAndPostSubscriptionRequest(
				Arrays.asList(neighbourA,neighbourB),
				Optional.of(LocalDateTime.now()),
				localSubscriptions,
				mockNeighbourFacade);


	}



	private void checkForNewNeighbours() {
		neighbourDiscoveryService.checkForNewNeighbours();
		List<Neighbour> unknown = repository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.UNKNOWN);
		assertThat(unknown).hasSize(2);
		assertThat(repository.findAll()).hasSize(2);
	}

	private void performCapabilityExchangeAndVerifyNeighbourRestFacadeCalls(Neighbour neighbour1, Neighbour neighbour2, Capabilities c1, Capabilities c2) {
		when(mockNeighbourFacade.postCapabilitiesToCapabilities(eq(neighbour1), any(), any())).thenReturn(c1);
		when(mockNeighbourFacade.postCapabilitiesToCapabilities(eq(neighbour2), any(), any())).thenReturn(c2);

		neighbourDiscoveryService.capabilityExchangeWithNeighbours(mockNeighbourFacade, Collections.emptySet(), Optional.empty());

		verify(mockNeighbourFacade, times(2)).postCapabilitiesToCapabilities(any(), any(), any());
		List<Neighbour> known = repository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.KNOWN);
		assertThat(known).hasSize(2);
	}

	private void performSubscriptionPolling(Neighbour neighbour, Subscription requestedSubscription) {
		when(mockNeighbourFacade.pollSubscriptionStatus(any(), any())).thenReturn(new Subscription(requestedSubscription.getSelector(), SubscriptionStatus.CREATED, ""));
		neighbourDiscoveryService.pollSubscriptions(mockNeighbourFacade);
		Neighbour found1 = repository.findByName(neighbour.getName());
		assertThat(found1).isNotNull();
		assertThat(found1.getOurRequestedSubscriptions()).isNotNull();
		assertThat(found1.getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);
		assertThat(found1.getOurRequestedSubscriptions().getSubscriptions().iterator().next().getSubscriptionStatus()).isEqualTo(SubscriptionStatus.CREATED);
	}


}
