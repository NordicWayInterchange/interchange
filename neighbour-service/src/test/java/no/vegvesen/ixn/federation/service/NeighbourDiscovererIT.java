package no.vegvesen.ixn.federation.service;


import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
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
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
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

	@Autowired
	ListenerEndpointRepository listenerEndpointRepository;

	private Set<LocalSubscription> localSubscriptions;

	@Test
	public void discovererIsAutowired() {
		assertThat(neighbourService).isNotNull();
	}

	@Test
	public void messageCollectorWillStartAfterCompleteOptimisticControlChannelFlow() {
		assertThat(repository.findAll()).withFailMessage("The test shall start with no neighbours stored. Use @Transactional.").hasSize(0);
		localSubscriptions = new HashSet<>();
		localSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and originatingCountry = 'NO'", nodeProperties.getName()));

		Neighbour neighbour1 = new Neighbour();
		neighbour1.setName("neighbour-one");
		Neighbour neighbour2 = new Neighbour();
		neighbour2.setName("neighbour-two");

		NeighbourCapabilities c1 = new NeighbourCapabilities(
				CapabilitiesStatus.KNOWN,
				Sets.newLinkedHashSet(new NeighbourCapability(
						new DatexApplication(
								"NO" + "-123",
								"NO" + "-pub",
								"NO",
								"1.0",
								List.of(),
								"SituationPublication"),
						new Metadata(RedirectStatus.OPTIONAL))));

		NeighbourCapabilities c2 = new NeighbourCapabilities(CapabilitiesStatus.KNOWN,
				Sets.newLinkedHashSet(new NeighbourCapability(
						new DatexApplication(
								"FI" + "-123",
								"FI" + "-pub",
								"FI",
								"1.0",
								List.of(),
								"SituationPublication"),
						new Metadata(RedirectStatus.OPTIONAL))));
		when(mockDnsFacade.lookupNeighbours()).thenReturn(Lists.list(neighbour1, neighbour2));

		checkForNewNeighbours();
		performCapabilityExchangeAndVerifyNeighbourRestFacadeCalls(neighbour1, neighbour2, c1, c2);
		String selector = String.format("messageType = %s and originatingCountry = 'NO'", Constants.DATEX_2);
		HashSet<Subscription> subscriptions = new HashSet<>(Arrays.asList(
				new Subscription(selector, SubscriptionStatus.ACCEPTED, "self"),
				new Subscription(selector, SubscriptionStatus.ACCEPTED, "self")
		));
		when(mockNeighbourFacade.postSubscriptionRequest(any(), anySet(), any())).thenReturn(subscriptions);

		neighbourDiscoveryService.evaluateAndPostSubscriptionRequest(Arrays.asList(neighbour1, neighbour2), Optional.ofNullable(lastUpdatedLocalSubscriptions), localSubscriptions, mockNeighbourFacade);

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
		assertThat(listenerEndpointRepository.findAll()).hasSize(0);
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

		neighbourService.incomingCapabilities(new CapabilitiesSplitApi("neighbour-one", Sets.newLinkedHashSet(new CapabilitySplitApi(new DatexApplicationApi("NO-213", "NO-pub", "NO", "1.0", List.of("0122"), "SituationPublication"), new MetadataApi(RedirectStatusApi.OPTIONAL)))), Collections.emptySet());
		List<Neighbour> toConsumeMessagesFrom = neighbourService.listNeighboursToConsumeMessagesFrom();
		assertThat(toConsumeMessagesFrom).hasSize(1);
	}

	@Test
	public void postEmptySubscriptionRequest() {
		neighbourDiscoveryService.postSubscriptionRequest(new Neighbour(),Collections.emptySet(),mockNeighbourFacade);
		assertThat(listenerEndpointRepository.findAll()).hasSize(0);
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
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						Collections.singleton(new NeighbourCapability(
								new DatexApplication(
										"NO0001",
										"pub-1",
										"NO",
										"1.0",
										List.of("0122"),
										"SituationPublication"
								),
								new Metadata(RedirectStatus.OPTIONAL)
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						Collections.singleton(
								new Subscription(
										"originatingCountry = 'NO'",
										SubscriptionStatus.CREATED,
										"bouvet.itsinterchange.eu"


								)
						)
				)
		);
		repository.save(neighbour);
		neighbourDiscoveryService.postSubscriptionRequest(neighbour, localSubscriptions,mockNeighbourFacade);
		verify(mockNeighbourFacade,never()).postSubscriptionRequest(any(Neighbour.class),anySet(),anyString());
		assertThat(listenerEndpointRepository.findAll()).hasSize(0);
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
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						Collections.singleton(new NeighbourCapability(
								new DatexApplication(
										"NO0001",
										"pub-1",
										"NO",
										"1.0",
										List.of("0122"),
										"SituationPublication"
								),
								new Metadata(RedirectStatus.OPTIONAL)
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
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
		assertThat(listenerEndpointRepository.findAll()).hasSize(0);
	}


	@Test
	public void oneLocalSubscriptionsBothMatchingTwoCapabilities() {
		Set<LocalSubscription> localSubscriptions = new HashSet<>(Arrays.asList(
				new LocalSubscription(
						LocalSubscriptionStatus.CREATED,
						"originatingCountry = 'NO'",
						""
				)
		));
		Neighbour neighbour = new Neighbour(
				"neighour",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						new HashSet<>(Arrays.asList(
								new NeighbourCapability(
										new DatexApplication(
												"NO0001",
												"pub-1",
												"NO",
												"1.0",
												List.of("0122"),
												"SituationPublication"
										),
										new Metadata(RedirectStatus.OPTIONAL)
								),
								new NeighbourCapability(
										new DenmApplication(
												"NO0001",
												"pub-2",
												"NO",
												"1.0",
												List.of("0122"),
												List.of(6)
									),
										new Metadata(RedirectStatus.OPTIONAL))
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
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
		assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);
		assertThat(listenerEndpointRepository.findAll()).hasSize(0);
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
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						Collections.singleton(new NeighbourCapability(
								new DatexApplication(
										"NO0001",
										"pub-1",
										"NO",
										"1.0",
										List.of("0122"),
										"SituationPublication"
								),
								new Metadata(RedirectStatus.OPTIONAL)
						)),
						LocalDateTime.now().minusHours(1)
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest()
		);
		neighbour.getCapabilities().setLastCapabilityExchange(LocalDateTime.now());
		repository.save(neighbour);
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
		assertThat(listenerEndpointRepository.findAll()).hasSize(0);
	}

	@Test
	public void evaluateAndPostSubscriptionRequestTwoNeighbours() {
		Set<LocalSubscription> localSubscriptions = Collections.singleton(
				new LocalSubscription(
						LocalSubscriptionStatus.CREATED,
						"originatingCountry = 'NO'",
						nodeProperties.getName()
				)
		);
		Neighbour neighbourA = new Neighbour(
				"neighbourA",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						Collections.singleton(new NeighbourCapability(
								new DatexApplication(
										"NO0001",
										"pub-1",
										"NO",
										"1.0",
										List.of("0122"),
										"SituationPublication"
								),
								new Metadata(RedirectStatus.OPTIONAL)
						)),
						LocalDateTime.now().minusHours(1)
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest()
		);
		neighbourA.getCapabilities().setLastCapabilityExchange(LocalDateTime.now());
		Neighbour neighbourB = new Neighbour(
				"neighbourB",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						Collections.singleton(new NeighbourCapability(
								new DatexApplication(
										"NO0002",
										"pub-2",
										"NO",
										"1.0",
										List.of("0122"),
										"SituationPublication"
								),
								new Metadata(RedirectStatus.OPTIONAL)
						)),
						LocalDateTime.now().minusHours(1)
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest()
		);
		Subscription responseSubscription1 = new Subscription(
				"originatingCountry = 'NO'",
				SubscriptionStatus.ACCEPTED,
				nodeProperties.getName()
		);

		Subscription responseSubscription2 = new Subscription(
				"originatingCountry = 'NO'",
				SubscriptionStatus.ACCEPTED,
				nodeProperties.getName()
		);

		neighbourB.getCapabilities().setLastCapabilityExchange(LocalDateTime.now());
		repository.save(neighbourA);
		repository.save(neighbourB);

		when(mockNeighbourFacade.postSubscriptionRequest(any(Neighbour.class), anySet(), anyString())).thenReturn(
				Collections.singleton(responseSubscription1), Collections.singleton(responseSubscription2)
		);

		neighbourDiscoveryService.evaluateAndPostSubscriptionRequest(
				Arrays.asList(neighbourA,neighbourB),
				Optional.of(LocalDateTime.now()),
				localSubscriptions,
				mockNeighbourFacade);

		assertThat(repository.findByName("neighbourA").getOurRequestedSubscriptions().getSubscriptions()).isNotEmpty();
		assertThat(repository.findByName("neighbourB").getOurRequestedSubscriptions().getSubscriptions()).isNotEmpty();
		assertThat(repository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.ACCEPTED)).hasSize(2);
		assertThat(listenerEndpointRepository.findAll()).hasSize(0);
		verify(mockNeighbourFacade, times(2)).postSubscriptionRequest(any(Neighbour.class), anySet(), anyString());
	}

	@Test
	public void postSubscriptionRequestWithTheSameLocalSubscriptionFromTwoServiceProviders() {
		String selector = "originatingCountry = 'NO' AND messageType = 'DENM'";
		String consumerCommonName = nodeProperties.getName();

		Set<LocalSubscription> localSubscriptions = new HashSet<>();

		LocalSubscription subscription1 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscriptions.add(subscription1);
		LocalSubscription subscription2 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscriptions.add(subscription2);

		Neighbour neighbour = new Neighbour(
				"neighbour",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						new HashSet<>(Collections.singletonList(
								new NeighbourCapability(
										new DenmApplication(
												"NO0001",
												"pub-1",
												"NO",
												"1.0",
												List.of("0122"),
												List.of(6)
										),
										new Metadata(RedirectStatus.OPTIONAL))
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest()
		);

		when(mockNeighbourFacade.postSubscriptionRequest(any(Neighbour.class),anySet(),anyString()))
				.thenReturn(new HashSet<>( Arrays.asList(
						new Subscription(
								selector,
								SubscriptionStatus.REQUESTED,
								nodeProperties.getName()
						),
						new Subscription(
								selector,
								SubscriptionStatus.REQUESTED,
								nodeProperties.getName())
						))

				);

		neighbourDiscoveryService.postSubscriptionRequest(neighbour, localSubscriptions, mockNeighbourFacade);
		verify(mockNeighbourFacade, times(1)).postSubscriptionRequest(any(Neighbour.class), anySet(), any(String.class));
		assertThat(repository.findByName("neighbour").getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);
		assertThat(listenerEndpointRepository.findAll()).hasSize(0);
	}

	@Test
	public void postSubscriptionRequestWithLocalSubscriptionRemovedTearDownListenerEndpoints() {
		String selector = "originatingCountry = 'NO' AND messageType = 'DENM'";
		String consumerCommonName = nodeProperties.getName();

		Subscription subscription = new Subscription(
				selector,
				SubscriptionStatus.CREATED,
				consumerCommonName
		);

		Endpoint endpoint = new Endpoint(
			"source",
				"host",
				5671
		);

		SubscriptionShard shard = new SubscriptionShard("target");

		endpoint.setShard(shard);
		subscription.setEndpoints(Collections.singleton(endpoint));

		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(
				"neighbour",
				"source",
				"host",
				5671,
				new Connection(),
				"target"
		);

		listenerEndpointRepository.save(listenerEndpoint);

		Neighbour neighbour = new Neighbour(
				"neighbour",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						new HashSet<>(Collections.singletonList(
								new NeighbourCapability(
										new DenmApplication(
												"NO0001",
												"pub-1",
												"NO",
												"1.0",
												List.of("0122"),
												List.of(6)
										),
										new Metadata(RedirectStatus.OPTIONAL))
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(Collections.singleton(subscription))
		);

		neighbourDiscoveryService.postSubscriptionRequest(neighbour, Collections.emptySet(), mockNeighbourFacade);

		assertThat(subscription.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.TEAR_DOWN);
		assertThat(subscription.getEndpoints()).hasSize(1);
		assertThat(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName("target", "source", "neighbour")).isNull();
	}

	@Test
	public void testGiveUp() {
		Neighbour neighbour = new Neighbour(
				"neighbour",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						new HashSet<>(Collections.singletonList(
								new NeighbourCapability(
										new DenmApplication(
												"NO0001",
												"pub-1",
												"NO",
												"1.0",
												List.of("0122"),
												List.of(6)
										),
										new Metadata(RedirectStatus.OPTIONAL))
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest()
		);

		String selector = "originatingCountry = 'NO' AND messageType = 'DENM'";
		String consumerCommonName = nodeProperties.getName();

		Set<LocalSubscription> localSubscriptions = new HashSet<>();

		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscriptions.add(subscription);

		ServiceProvider serviceProvider = new ServiceProvider("serviceprovider");
		serviceProvider.addLocalSubscription(subscription);

		when(mockNeighbourFacade.postSubscriptionRequest(any(Neighbour.class),anySet(),anyString()))
				.thenReturn(new HashSet<>( Arrays.asList(
						new Subscription(
								selector,
								SubscriptionStatus.REQUESTED,
								nodeProperties.getName()
						)
				)));

		neighbourDiscoveryService.postSubscriptionRequest(neighbour, localSubscriptions, mockNeighbourFacade);
		assertThat(repository.findByName("neighbour").getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);

		when(mockNeighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class)))
				.thenReturn(new Subscription(
						selector,
						SubscriptionStatus.CREATED,
						nodeProperties.getName()
				));

		neighbourDiscoveryService.pollSubscriptionsWithStatusCreated(mockNeighbourFacade);
		assertThat(repository.findByName("neighbour").getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);

		when(mockNeighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class)))
				.thenThrow(new SubscriptionPollException("Subscription poll gone wrong"));

		neighbourDiscoveryService.pollSubscriptionsWithStatusCreatedOneNeighbour(neighbour, mockNeighbourFacade);

		for (int i=0; i<8; i++) {
			neighbour.getControlConnection().setLastFailedConnectionAttempt(LocalDateTime.now().minusHours(3));
			neighbour.getControlConnection().setBackoffStart(LocalDateTime.now().minusHours(1));
			neighbourDiscoveryService.pollSubscriptions(mockNeighbourFacade);
		}

		neighbour.setCapabilities(new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Collections.emptySet(), LocalDateTime.now()));

		neighbourDiscoveryService.retryUnreachable(mockNeighbourFacade, Capability.transformNeighbourCapabilityToSplitCapability(neighbour.getCapabilities().getCapabilities()));

		neighbourDiscoveryService.postSubscriptionRequest(neighbour, localSubscriptions, mockNeighbourFacade);
		assertThat(repository.findByName("neighbour").getOurRequestedSubscriptions().getSubscriptionsByStatus(SubscriptionStatus.TEAR_DOWN)).hasSize(1);
		assertThat(listenerEndpointRepository.findAll()).hasSize(0);
	}

	@Test
	public void subscriptionIsSetToTearDownWhenEndpointsListFromNeighbourChanges() {
		String selector = "originatingCountry = 'NO' AND messageType = 'DENM' AND publisherId = 'NO00001'";
		String consumerCommonName = nodeProperties.getName();

		Subscription sub = new Subscription(
				selector,
				SubscriptionStatus.CREATED,
				consumerCommonName
		);

		Endpoint endpoint = new Endpoint(
				"neighbour-endpoints-source",
				"neighbour-endpoints",
				5671
		);

		SubscriptionShard shard = new SubscriptionShard("neighbour-endpoints-target");
		endpoint.setShard(shard);

		sub.setEndpoints(Collections.singleton(endpoint));

		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(
				"neighbour-endpoints",
				"neighbour-endpoints-source",
				"neighbour-endpoints",
				5671,
				new Connection(),
				"neighbour-endpoints-target"
		);

		Neighbour neighbour = new Neighbour(
				"neighbour-endpoints",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						new HashSet<>(Collections.singletonList(
								new NeighbourCapability(
										new DenmApplication(
												"NO0001",
												"pub-1",
												"NO",
												"1.0",
												List.of(),
												List.of()
										),
										new Metadata(RedirectStatus.OPTIONAL))
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						Collections.singleton(
								sub
						)
				)
		);

		repository.save(neighbour);
		listenerEndpointRepository.save(listenerEndpoint);

		Subscription polledSubscription = new Subscription(
				selector,
				SubscriptionStatus.CREATED,
				nodeProperties.getName()
		);

		Endpoint polledEndpoint = new Endpoint(
				"polled-neighbour-endpoints-source",
				"neighbour-endpoints",
				5671
		);

		polledSubscription.setEndpoints(Collections.singleton(polledEndpoint));

		when(mockNeighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class)))
				.thenReturn(
						polledSubscription
				);

		neighbourDiscoveryService.pollSubscriptionsWithStatusCreated(mockNeighbourFacade);

		assertThat(sub.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.TEAR_DOWN);
		assertThat(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName("neighbour-endpoints-target", "neighbour-endpoints-source", "neighbour-endpoints")).isNull();
	}

	@Test
	public void listenerEndpointsAreRemovedWhenSubscriptionPollReturnsFailedPollForCreatedSubscription() {
		String selector = "originatingCountry = 'NO' AND messageType = 'DENM' AND publisherId = 'NO00001'";
		String consumerCommonName = nodeProperties.getName();

		Subscription sub = new Subscription(
				selector,
				SubscriptionStatus.CREATED,
				consumerCommonName
		);

		Endpoint endpoint = new Endpoint(
				"neighbour-endpoints-failed-source",
				"neighbour-endpoints-failed",
				5671
		);

		SubscriptionShard shard = new SubscriptionShard("neighbour-endpoints-failed-target");
		endpoint.setShard(shard);

		sub.setEndpoints(Collections.singleton(endpoint));

		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(
				"neighbour-endpoints-failed",
				"neighbour-endpoints-failed-source",
				"neighbour-endpoints-failed",
				5671,
				new Connection(),
				"neighbour-endpoints-failed-target"
		);

		Neighbour neighbour = new Neighbour(
				"neighbour-endpoints-failed",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						new HashSet<>(Collections.singletonList(
								new NeighbourCapability(
										new DenmApplication(
												"NO0001",
												"pub-1",
												"NO",
												"1.0",
												List.of(),
												List.of()
										),
										new Metadata(RedirectStatus.OPTIONAL))
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						Collections.singleton(
								sub
						)
				)
		);

		repository.save(neighbour);
		listenerEndpointRepository.save(listenerEndpoint);

		when(mockNeighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class)))
				.thenThrow(new SubscriptionPollException("Subscription poll gone wrong"));

		neighbourDiscoveryService.pollSubscriptionsWithStatusCreated(mockNeighbourFacade);

		assertThat(sub.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.FAILED);
		assertThat(sub.getEndpoints()).isNotEmpty();
		assertThat(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName("neighbour-endpoints-failed-target", "neighbour-endpoints-failed-source", "neighbour-endpoints-failed")).isNull();
	}

	@Test
	public void testBackoffIsIncreasedEverytimeASubscriptionFailsWithConnectionFailure() {
		String selector1 = "originatingCountry = 'NO' AND messageType = 'DENM' AND publisherId = 'NO00001'";
		String selector2 = "originatingCountry = 'NO' AND messageType = 'DENM' AND publicationId = 'pub-1'";
		String selector3 = "originatingCountry = 'NO' AND messageType = 'DENM' AND protocolVersion = '1.0'";
		String consumerCommonName = nodeProperties.getName();

		Neighbour neighbour = new Neighbour(
				"neighbour",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						new HashSet<>(Collections.singletonList(
								new NeighbourCapability(
										new DenmApplication(
												"NO0001",
												"pub-1",
												"NO",
												"1.0",
												List.of("0122"),
												List.of(6)
										),
										new Metadata(RedirectStatus.OPTIONAL))
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						new HashSet<>(Arrays.asList(
								new Subscription(
										selector1,
										SubscriptionStatus.CREATED,
										consumerCommonName
								), new Subscription(
										selector2,
										SubscriptionStatus.CREATED,
										consumerCommonName
								), new Subscription(
										selector3,
										SubscriptionStatus.CREATED,
										consumerCommonName
								)
						))
				)
		);

		when(mockNeighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class)))
				.thenThrow(new SubscriptionPollException("Subscription poll gone wrong"));

		neighbourDiscoveryService.pollSubscriptionsWithStatusCreatedOneNeighbour(neighbour, mockNeighbourFacade);
		assertThat(repository.findByName("neighbour").getControlConnection().getBackoffAttempts()).isEqualTo(2);
	}

	@Test
	public void testResetOfBackoffCounterWhenPollIsSuccessfulAgainAfterFailure() {
		String selector = "originatingCountry = 'NO' AND messageType = 'DENM' AND publisherId = 'NO00002'";
		String consumerCommonName = nodeProperties.getName();

		Subscription sub = new Subscription(
				selector,
				SubscriptionStatus.FAILED,
				consumerCommonName
		);
		sub.setNumberOfPolls(1);

		Neighbour neighbour = new Neighbour(
				"neighbour1",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						new HashSet<>(Collections.singletonList(
								new NeighbourCapability(
										new DenmApplication(
												"NO0002",
												"pub-1",
												"NO",
												"1.0",
												List.of("0122"),
												List.of(6)
										),
										new Metadata(RedirectStatus.OPTIONAL))
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						Collections.singleton(
								sub
						)
				)
		);

		neighbour.getControlConnection().setBackoffStart(LocalDateTime.now());
		neighbour.getControlConnection().setBackoffAttempts(0);
		neighbour.getControlConnection().setConnectionStatus(ConnectionStatus.FAILED);

		when(mockNeighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class)))
				.thenReturn(
						new Subscription(
								selector,
								SubscriptionStatus.CREATED,
								nodeProperties.getName()
						)
				);

		neighbourDiscoveryService.pollSubscriptionsOneNeighbour(neighbour, mockNeighbourFacade);
		assertThat(repository.findByName("neighbour1").getControlConnection().getBackoffStartTime()).isNull();
	}

	@Test
	public void subscriptionPollWithNotFoundInReturnRemovesListenerEndpoints() {
		String selector = "originatingCountry = 'NO' AND messageType = 'DENM'";
		String consumerCommonName = nodeProperties.getName();

		Subscription sub = new Subscription(
				selector,
				SubscriptionStatus.FAILED,
				consumerCommonName
		);

		Endpoint endpoint = new Endpoint(
				"source",
				"neighbour",
				5671
		);

		SubscriptionShard shard = new SubscriptionShard("target");
		endpoint.setShard(shard);

		sub.setEndpoints(Collections.singleton(endpoint));

		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(
				"neighbour",
				"source",
				"neighbour",
				5671,
				new Connection(),
				"target"
		);

		Neighbour neighbour = new Neighbour(
				"neighbour",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						new HashSet<>(Collections.singletonList(
								new NeighbourCapability(
										new DenmApplication(
												"NO0001",
												"pub-1",
												"NO",
												"1.0",
												List.of(),
												List.of()
										),
										new Metadata(RedirectStatus.OPTIONAL))
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						Collections.singleton(
								sub
						)
				)
		);

		listenerEndpointRepository.save(listenerEndpoint);

		when(mockNeighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenThrow(new SubscriptionNotFoundException("Not found"));
		neighbourDiscoveryService.pollSubscriptionsOneNeighbour(neighbour, mockNeighbourFacade);

		assertThat(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName("target", "source", "neighbour")).isNull();
		assertThat(sub.getEndpoints()).hasSize(1);
		assertThat(sub.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.TEAR_DOWN);
	}

	@Test
	public void subscriptionPollWithFailedInReturnRemovesListenerEndpoints() {
		String selector = "originatingCountry = 'NO' AND messageType = 'DENM'";
		String consumerCommonName = nodeProperties.getName();

		Subscription sub = new Subscription(
				selector,
				SubscriptionStatus.FAILED,
				consumerCommonName
		);

		Endpoint endpoint = new Endpoint(
				"source",
				"neighbour",
				5671
		);

		SubscriptionShard shard = new SubscriptionShard("target");
		endpoint.setShard(shard);

		sub.setEndpoints(Collections.singleton(endpoint));

		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(
				"neighbour",
				"source",
				"neighbour",
				5671,
				new Connection(),
				"target"
		);

		Neighbour neighbour = new Neighbour(
				"neighbour",
				new NeighbourCapabilities(
						CapabilitiesStatus.KNOWN,
						new HashSet<>(Collections.singletonList(
								new NeighbourCapability(
										new DenmApplication(
												"NO0001",
												"pub-1",
												"NO",
												"1.0",
												List.of(),
												List.of()
										),
										new Metadata(RedirectStatus.OPTIONAL))
						)),
						LocalDateTime.now()
				),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						Collections.singleton(
								sub
						)
				)
		);

		listenerEndpointRepository.save(listenerEndpoint);

		when(mockNeighbourFacade.pollSubscriptionStatus(any(Subscription.class), any(Neighbour.class))).thenThrow(new SubscriptionPollException("Error in poll"));
		neighbourDiscoveryService.pollSubscriptionsOneNeighbour(neighbour, mockNeighbourFacade);

		assertThat(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName("target", "source", "neighbour")).isNull();
		assertThat(sub.getEndpoints()).hasSize(1);
		assertThat(sub.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.FAILED);
	}

	@Test
	public void neighbourIsIgnoredDuringCapabilityExchange(){
		String name = "ignoredNeighbour1";
		Neighbour neighbour = ignoredNeighbour(name);
		neighbourDiscoveryService.capabilityExchangeWithNeighbours(mockNeighbourFacade, Collections.emptySet(), Optional.of(LocalDateTime.now()));
		verify(mockNeighbourFacade, times(0)).postCapabilitiesToCapabilities(any(),any(),any());
	}

	@Test
	public void neighbourIsIgnoredWhenPostingSubscriptionRequest(){
		String name = "ignoredNeighbour2";
		Neighbour neighbour = ignoredNeighbour(name);
		neighbour.getCapabilities().addDataType(new CapabilitySplit(
				new DatexApplication(),
				new Metadata()
		));
		neighbourDiscoveryService.evaluateAndPostSubscriptionRequest(
                List.of(neighbour),
				Optional.of(LocalDateTime.now()),
				localSubscriptions,
				mockNeighbourFacade);
		verify(mockNeighbourFacade, times(0)).postSubscriptionRequest(any(),any(),any());
	}

	@Test
	public void neighbourIsIgnoredWhenPollingSubscription(){
		String name = "ignoredNeighbour3";
		Neighbour neighbour = ignoredNeighbour(name);
		neighbour.setOurRequestedSubscriptions(new SubscriptionRequest(Set.of(new Subscription("test", SubscriptionStatus.REQUESTED))));
		neighbourDiscoveryService.pollSubscriptions(mockNeighbourFacade);
		verify(mockNeighbourFacade, times(0)).pollSubscriptionStatus(any(),any());
	}

	private void checkForNewNeighbours() {
		neighbourDiscoveryService.checkForNewNeighbours();
		List<Neighbour> unknown = repository.findByCapabilities_Status(CapabilitiesStatus.UNKNOWN);
		assertThat(unknown).hasSize(2);
		assertThat(repository.findAll()).hasSize(2);
	}

	private void performCapabilityExchangeAndVerifyNeighbourRestFacadeCalls(Neighbour neighbour1, Neighbour neighbour2, NeighbourCapabilities c1, NeighbourCapabilities c2) {
		when(mockNeighbourFacade.postCapabilitiesToCapabilities(eq(neighbour1), any(), any())).thenReturn(c1.getCapabilities());
		when(mockNeighbourFacade.postCapabilitiesToCapabilities(eq(neighbour2), any(), any())).thenReturn(c2.getCapabilities());

		neighbourDiscoveryService.capabilityExchangeWithNeighbours(mockNeighbourFacade, Collections.emptySet(), Optional.empty());

		verify(mockNeighbourFacade, times(2)).postCapabilitiesToCapabilities(any(), any(), any());
		List<Neighbour> known = repository.findByCapabilities_Status(CapabilitiesStatus.KNOWN);
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

	public Neighbour ignoredNeighbour(String name){
		Neighbour neighbour = new Neighbour(
				name,
				new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Set.of(
						new CapabilitySplit(
								new DatexApplication(),
								new Metadata()
						)
				)),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest()
		);
		neighbour.setIgnore(true);
		repository.save(neighbour);
		return neighbour;
	}
}
