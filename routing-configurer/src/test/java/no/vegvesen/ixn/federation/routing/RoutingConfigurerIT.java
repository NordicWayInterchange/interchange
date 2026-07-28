package no.vegvesen.ixn.federation.routing;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.selector.MessageValidatingSelectorCreator;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.service.routing.match.MatchDiscoveryService;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import no.vegvesen.ixn.shared.Constants;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Testcontainers
public class RoutingConfigurerIT extends QpidDockerBaseIT {

	public static final String HOST_NAME = getDockerHost();
	private static final String NEIGHBOUR = "neighbour1";
	public static CaStores stores = generateStores(getTargetFolderPathForTestClass(RoutingConfigurerIT.class),"my_ca", HOST_NAME,"routing_configurer","king_gustaf","nordea",NEIGHBOUR);


	private static final Logger logger = LoggerFactory.getLogger(RoutingConfigurerIT.class);

	@Container
	public static PostgreSQLContainer<?> postgreSQLContainer = new  PostgreSQLContainer<>("postgres:18.1")
			.withDatabaseName("federation")
			.withUsername("federation")
			.withPassword("federation");

	@Container
	public static final QpidContainer qpidContainer = getQpidTestContainer(
			stores,
			HOST_NAME,
			HOST_NAME,
			Path.of("qpid")
	);


	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		qpidContainer.followOutput(new Slf4jLogConsumer(logger));
		String httpsUrl = qpidContainer.getHttpsUrl();
		String httpUrl = qpidContainer.getHttpUrl();
		logger.info("server url: {}", httpsUrl);
		logger.info("server url: {}", httpUrl);
		ClusterKeyGenerator.ClientStore routingConfigurerStore = ClusterKeyGenerator.getClientStore("routing_configurer", stores.clientStores().stream());
		ClusterKeyGenerator.CaStore caStore = stores.trustStore();
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
		registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
		registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
		registry.add("spring.jpa.hibernate.ddl-auto", ()-> "create-drop");
		registry.add("routing-configurer.interval",()->"999");
		registry.add("routing-configurer.baseUrl", qpidContainer::getHttpsUrl);
		registry.add("routing-configurer.vhost",() -> HOST_NAME);
		registry.add("interchange.node-provider.name", () -> HOST_NAME);
		registry.add("spring.ssl.bundle.jks.qpid-client.keystore.location", () -> routingConfigurerStore.path().toString());
		registry.add("spring.ssl.bundle.jks.qpid-client.keystore.password", routingConfigurerStore::password);
		registry.add("spring.ssl.bundle.jks.qpid-client.truststore.location", () -> caStore.truststoreName().toString());
		registry.add("spring.ssl.bundle.jks.qpid-client.truststore.password", caStore::truststorePassword);
	}

	private final NeighbourCapabilities emptyNeighbourCapabilities = new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet());
	private final SubscriptionRequest emptySubscriptionRequest = new SubscriptionRequest(emptySet());


	@Autowired
	NeighbourService neighbourService;

	@Autowired
	MatchDiscoveryService matchDiscoveryService;

	@Autowired
	RoutingConfigurer routingConfigurer;

	@Autowired
	QpidClient client;

	@Autowired
	ServiceProviderRepository serviceProviderRepository;

	@Autowired
	ListenerEndpointRepository listenerEndpointRepository;

	@Autowired
	OutgoingMatchRepository outgoingMatchRepository;

	@Autowired
    NeighbourRepository neighbourRepository;

	@Autowired
	InterchangeNodeProperties interchangeNodeProperties;

	@Test
	public void neighbourWithOneSubscriptionIsCreated() {
		String capabilityExchange = "cap-ex1";
		Capability cap = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, capabilityExchange);
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange(capabilityExchange);

		ServiceProvider sp = new ServiceProvider("sp", new Capabilities(Set.of(cap)));
		serviceProviderRepository.save(sp);

		NeighbourSubscription subscription = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "flounder");
		Set<NeighbourSubscription> subscriptions = Set.of(subscription);

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
		Neighbour flounder = new Neighbour("flounder", emptyNeighbourCapabilities, subscriptionRequest, emptySubscriptionRequest);
		neighbourRepository.save(flounder);

		routingConfigurer.setupNeighbourRouting(flounder, client.getQpidDelta());
		String source = subscription.getEndpoints().stream().findFirst().orElseThrow().getSource();
		assertThat(client.queueExists(source)).isTrue();
		assertThat(client.getQueuePublishingLinks(source)).hasSize(1);
		assertThat(subscription.getLastUpdatedTimestamp()).isGreaterThan(0);
		assertThat(subscription.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
	}

	@Test
	public void neighbourWithTwoSubscriptionsIsCreated() {
		String capabilityExchange = "cap-ex2";
		Capability cap1 = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, capabilityExchange);
		cap1.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange(capabilityExchange);

		Capability cap2 = getDatexCapability("pub-2", RedirectStatus.OPTIONAL, "cap-ex3");
		cap2.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex3");

		ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap1, cap2)));
		serviceProviderRepository.save(sp);

		NeighbourSubscription s1 = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "halibut");

		NeighbourSubscription s2 = new NeighbourSubscription("publicationId = 'pub-2'", NeighbourSubscriptionStatus.ACCEPTED, "halibut");

		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
		Neighbour halibut = new Neighbour("halibut", emptyNeighbourCapabilities, subscriptionRequest, emptySubscriptionRequest);
		neighbourRepository.save(halibut);

		routingConfigurer.setupNeighbourRouting(halibut, client.getQpidDelta());
		String source1 = s1.getEndpoints().stream().findFirst().orElseThrow().getSource();
		assertThat(client.queueExists(source1)).isTrue();
		assertThat(client.getQueuePublishingLinks(source1)).hasSize(1);
		String source2 = s2.getEndpoints().stream().findFirst().orElseThrow().getSource();
		assertThat(client.queueExists(source2)).isTrue();
		assertThat(client.getQueuePublishingLinks(source2)).hasSize(1);
		assertThat(s1.getLastUpdatedTimestamp()).isGreaterThan(0);
		assertThat(s2.getLastUpdatedTimestamp()).isGreaterThan(0);
		assertThat(s1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(s2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
	}


        @Test
        public void neighbourWithTwoSubscriptionsAndOnlyOneAcceptedIsCreated() {
            Capability cap1 = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex4");
            cap1.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex4");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap1)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription s1 = new NeighbourSubscription("publicationId = 'pub-1' AND quadTree like '%,01230123%'", NeighbourSubscriptionStatus.ACCEPTED, "salmon");

            NeighbourSubscription s2 = new NeighbourSubscription("publicationId = 'pub-1' AND quadTree like '%,01230122%'", NeighbourSubscriptionStatus.ILLEGAL, "salmon");

            //Just to ensure the timestamp is updated by RoutingConfigurer
            assertThat(s1.getLastUpdatedTimestamp()).isEqualTo(0);
            assertThat(s2.getLastUpdatedTimestamp()).isEqualTo(0);

            Set<NeighbourSubscription> subscriptions = Set.of(s1, s2);
            NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
            Neighbour salmon = new Neighbour("salmon", emptyNeighbourCapabilities, subscriptionRequest, emptySubscriptionRequest);
			neighbourRepository.save(salmon);

            routingConfigurer.setupNeighbourRouting(salmon, client.getQpidDelta());
            assertThat(client.queueExists(s1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(s1.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
            assertThat(s2.getEndpoints()).isEmpty();
            assertThat(s1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(s2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.ILLEGAL);

            //Showing that the timestamp have been changed for the ACCEPTED subscription, but not for the REJECTED one
            assertThat(s1.getLastUpdatedTimestamp()).isGreaterThan(0);
            assertThat(s2.getLastUpdatedTimestamp()).isEqualTo(0);
        }

        @Test
        public void neighbourToreDownWillBeRemovedFromFederatedInterchangesGroup() {
            Capability cap = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex5");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex5");

            ServiceProvider serviceProvider = new ServiceProvider("my-sp",new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(serviceProvider);

            NeighbourSubscription neighbourSub = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "tore-down-neighbour");

            Neighbour toreDownNeighbour = new Neighbour("tore-down-neighbour", emptyNeighbourCapabilities, new NeighbourSubscriptionRequest(Collections.singleton(neighbourSub)), emptySubscriptionRequest);
			neighbourRepository.save(toreDownNeighbour);
            routingConfigurer.setupNeighbourRouting(toreDownNeighbour, client.getQpidDelta());
            assertThat(client.getNeighbourMember(toreDownNeighbour.getName())).isNotNull();

            neighbourSub.setSubscriptionStatus(NeighbourSubscriptionStatus.TEAR_DOWN);
			neighbourRepository.save(toreDownNeighbour);

            routingConfigurer.tearDownNeighbourRouting(toreDownNeighbour);
            assertThat(client.getNeighbourMember(toreDownNeighbour.getName())).isNull();
        }


        @Test
        public void addingOneSubscriptionAndTwoCapabilitiesResultsInOneEndpointAndTwoBindings() {
            Capability cap1 = getDatexCapability("pub-1", RedirectStatus.NOT_AVAILABLE, "cap-ex6");
            cap1.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex6");

            Capability cap2 = getDatexCapability("pub-2", RedirectStatus.NOT_AVAILABLE, "cap-ex7");
            cap2.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex7");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap1, cap2)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1' OR publicationId = 'pub-2'", NeighbourSubscriptionStatus.ACCEPTED, "tigershark");

            Neighbour tigershark = new Neighbour("tigershark", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), emptySubscriptionRequest);
			neighbourRepository.save(tigershark);

            routingConfigurer.setupNeighbourRouting(tigershark, client.getQpidDelta());
            assertThat(sub.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().get().getSource())).hasSize(2);
            assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(tigershark.getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(1);
        }


        @Test
        public void routingIsNotSetUpWhenRedirectIsNotAvailable() {
            Capability cap = getDatexCapability("pub-1", RedirectStatus.NOT_AVAILABLE, "cap-ex8");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex8");

            NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "remote-service-provider");

            Neighbour cod = new Neighbour("cod", emptyNeighbourCapabilities, new NeighbourSubscriptionRequest(Collections.singleton(sub)), emptySubscriptionRequest);
			neighbourRepository.save(cod);

            ServiceProvider serviceProvider = new ServiceProvider("sp",new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(serviceProvider);

            routingConfigurer.setupNeighbourRouting(cod, client.getQpidDelta());
            assertThat(client.queueExists(cod.getName())).isFalse();
            assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.NO_OVERLAP);
            assertThat(sub.getEndpoints()).isEmpty();
        }


        @Test
        public void setUpQueueForServiceProviderAndNeighbourForOneCapability() {
            Capability cap = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex9");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex9");

            Capabilities capabilities = new Capabilities(singleton(cap));
            ServiceProvider sp = new ServiceProvider("sp",capabilities);
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub1 = new NeighbourSubscription("publicationId = 'pub-1' AND quadTree like '%,01230123%'", NeighbourSubscriptionStatus.ACCEPTED, "remote-sp");
            NeighbourSubscription sub2 = new NeighbourSubscription("publicationId = 'pub-1' AND quadTree like '%,01230122%'", NeighbourSubscriptionStatus.ACCEPTED, "neigh-both");

            Neighbour neigh = new Neighbour("neigh-both", new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(new HashSet<>(Arrays.asList(sub1, sub2))), emptySubscriptionRequest);
			neighbourRepository.save(neigh);

            routingConfigurer.setupNeighbourRouting(neigh, client.getQpidDelta());
            assertThat(sub1.getEndpoints()).hasSize(1);
            assertThat(sub2.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.queueExists(sub2.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(sub1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(sub2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
        }


        @Test
        public void setupRoutingWithCapabilityExchanges() throws Exception {
			String serviceProviderName = "king_gustaf";
			LocalDelivery delivery = new LocalDelivery(
                    "originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = 6",
                    LocalDeliveryStatus.CREATED,
                    "DENM Delivery",
                    false
            );
			String deliveryExchangeName = "del-ex10";

			CapabilityShard shard = new CapabilityShard(1, "cap-ex10", "publicationId = 'pub-1'");
			Capability cap = new Capability(
                    new DenmApplication(
                            "NO-123",
                            "pub-1",
                            "NO",
                            "DENM:1.2.2",
                            List.of("12004"),
                            List.of(6)
                    ),
                    new Metadata(RedirectStatus.OPTIONAL),
                    List.of(shard)
            );
			cap.setStatus(CapabilityStatus.CREATED);
			client.createHeadersExchange("cap-ex10");

			ServiceProvider sp = new ServiceProvider(serviceProviderName, new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(sp);

			String joinedSelector = String.format("(%s) AND (%s)", delivery.getSelector(), MessageValidatingSelectorCreator.makeSelector(cap, null));

			client.createHeadersExchange(deliveryExchangeName);
			client.addWriteAccess(sp.getName(), deliveryExchangeName);
			client.addBinding(deliveryExchangeName, new Binding(deliveryExchangeName, cap.getShards().stream().findFirst().orElseThrow().getExchangeName(), new Filter(joinedSelector)));

			NeighbourSubscription sub = new NeighbourSubscription("originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = 6", NeighbourSubscriptionStatus.ACCEPTED, NEIGHBOUR);

			Set<NeighbourSubscription> subs = Set.of(sub);

			Neighbour neigh = new Neighbour(NEIGHBOUR, new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);
			neighbourRepository.save(neigh);

			routingConfigurer.setupNeighbourRouting(neigh, client.getQpidDelta());
			assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().orElseThrow().getSource())).isTrue();

	        SSLContext serviceProviderContext = sslClientContext(stores, serviceProviderName);
			SSLContext neighbourContext = sslClientContext(stores,neigh.getName());
            AtomicInteger numMessages = new AtomicInteger();
            try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(),
                    sub.getEndpoints().stream().findFirst().orElseThrow().getSource(),
                    neighbourContext,
                    message -> numMessages.incrementAndGet())) {
                sink.start();
                try (Source source = new Source(qpidContainer.getAmqpsUrl(),deliveryExchangeName,serviceProviderContext)) {
                    source.start();
                    String messageText = "This is my DENM message :) ";
                    byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
                    source.sendNonPersistentMessage(source.createMessageBuilder()
                            .bytesMessage(bytemessage)
                            .userId("kong_olav")
                            .publisherId("NO-123")
                            .publicationId("pub-1")
                            .messageType(Constants.DENM)
                            .causeCode(6)
                            .subCauseCode(61)
                            .originatingCountry("NO")
                            .protocolVersion("DENM:1.2.2")
                            .quadTreeTiles(",12004,")
                            .shardId(1)
                            .shardCount(1)
                            .timestamp(System.currentTimeMillis())
                            .build());
                }
                Thread.sleep(200);
            }
            assertThat(numMessages.get()).isEqualTo(1);
        }

        @Test
        public void oneShardedCapabilityAndOneShardedSubscription() {
            Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex11", "cap-ex12", "cap-ex13");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex11");
            client.createHeadersExchange("cap-ex12");
            client.createHeadersExchange("cap-ex13");

            ServiceProvider sp = new ServiceProvider("sp", new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
            assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
        }

        @Test
        public void oneShardedCapabilityAndTwoShardedSubscriptions() {
            Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex14", "cap-ex15", "cap-ex16");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex14");
            client.createHeadersExchange("cap-ex15");
            client.createHeadersExchange("cap-ex16");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub1 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");
            NeighbourSubscription sub2 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(new HashSet<>(Arrays.asList(sub1, sub2))), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub1.getEndpoints()).hasSize(1);
            assertThat(sub2.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub1.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
            assertThat(client.queueExists(sub2.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub2.getEndpoints().stream().findFirst().get().getSource())).hasSize(2);
            assertThat(sub1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(sub2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
        }


        @Test
        public void oneShardedCapabilityAndNotShardedSubscription() {
            Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex17", "cap-ex18", "cap-ex19");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex17");
            client.createHeadersExchange("cap-ex18");
            client.createHeadersExchange("cap-ex19");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().get().getSource())).hasSize(3);
            assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
        }

        @Test
        public void oneCapabilityNotShardedAndOneSubscriptionSharded() {
            Capability cap = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex20");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex20");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub.getEndpoints()).hasSize(0);
            assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.NO_OVERLAP);
        }

        @Test
        public void oneShardedCapabilityAndOneSubscriptionShardedAndOneSubscriptionNotSharded() {
            Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex21", "cap-ex22", "cap-ex23");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex21");
            client.createHeadersExchange("cap-ex22");
            client.createHeadersExchange("cap-ex23");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub1 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");
            NeighbourSubscription sub2 = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(new HashSet<>(Arrays.asList(sub1, sub2))), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub1.getEndpoints()).hasSize(1);
            assertThat(sub2.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub1.getEndpoints().stream().findFirst().get().getSource())).hasSize(2);
            assertThat(client.queueExists(sub2.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub2.getEndpoints().stream().findFirst().get().getSource())).hasSize(3);
            assertThat(sub1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(sub2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
        }

        @Test
        public void twoCapabilitiesShardedAndOneSubscriptionSharded() {
            Capability cap1 = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex24", "cap-ex25", "cap-ex26");
            cap1.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex24");
            client.createHeadersExchange("cap-ex25");
            client.createHeadersExchange("cap-ex26");

            Capability cap2 = getShardedCapability("pub-2", RedirectStatus.OPTIONAL, "cap-ex27", "cap-ex28", "cap-ex29");
            cap2.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex27");
            client.createHeadersExchange("cap-ex28");
            client.createHeadersExchange("cap-ex29");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap1, cap2)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub = new NeighbourSubscription("(publicationId = 'pub-1' OR publicationId = 'pub-2') AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().orElseThrow().getSource())).hasSize(4);
            assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
        }

        @Test
        public void twoCapabilitiesOneShardedAndOneNotAndOneShardedSubscription() {
            Capability cap1 = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex49");
            cap1.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex49");

            Capability cap2 = getShardedCapability("pub-2", RedirectStatus.OPTIONAL, "cap-ex50", "cap-ex51", "cap-ex52");
            cap2.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex50");
            client.createHeadersExchange("cap-ex51");
            client.createHeadersExchange("cap-ex52");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap1, cap2)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub = new NeighbourSubscription("(publicationId = 'pub-1' OR publicationId = 'pub-2') AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().get().getSource())).hasSize(2);
            assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
        }

        @Test
        public void subscriptionShardIsNotSetUpWhenEndpointIsMissing() {
            String selector = "a=b";
            Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED);
            subscription.setConsumerCommonName("my-node");

            Neighbour myNeighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Set.of(subscription)));
			neighbourRepository.save(myNeighbour);

            routingConfigurer.setUpSubscriptionExchanges();

            assertThat(subscription.getEndpoints()).isEmpty();
        }

        @Test
        public void setUpSubscriptionShardExchange() {
            String selector = "a=b";
            Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED);
            subscription.setConsumerCommonName(interchangeNodeProperties.getName());
            subscription.setEndpoints(singleton(new Endpoint("my-source", "my-host", 5671)));

            Neighbour myNeighbour = new Neighbour("neighbour", new NeighbourCapabilities(),new NeighbourSubscriptionRequest() ,new SubscriptionRequest(singleton(subscription)));
			neighbourRepository.save(myNeighbour);

            routingConfigurer.setUpSubscriptionExchanges();

            assertThat(subscription.getEndpoints().stream().findFirst().orElseThrow().hasShard()).isTrue();
            assertThat(client.exchangeExists(subscription.getEndpoints().stream().findFirst().orElseThrow().getShard().getExchangeName())).isTrue();
        }

        @Test
        public void multipleEndpointsGetTheirOwnShardAndListenerEndpointsAreCreated() {
            String selector = "a=b";
			Endpoint end1 = new Endpoint("my-source1", "my-host", 5671);
			Endpoint end2 = new Endpoint("my-source2", "my-host", 5671);
			Subscription subscription = new Subscription(
					SubscriptionStatus.CREATED,
					selector,
					"/subs1",
					interchangeNodeProperties.getName(),
					Set.of(end1, end2)
			);

            Neighbour myNeighbour = new Neighbour("Neighbour", new NeighbourCapabilities(),new NeighbourSubscriptionRequest(),new SubscriptionRequest(singleton(subscription)));
			neighbourRepository.save(myNeighbour);

            routingConfigurer.setUpSubscriptionExchanges();

            assertThat(end1.hasShard()).isTrue();
            assertThat(end2.hasShard()).isTrue();
            assertThat(client.exchangeExists(end1.getShard().getExchangeName())).isTrue();
            assertThat(client.exchangeExists(end2.getShard().getExchangeName())).isTrue();
        }

        @Test
        public void createNeighbourWithSubscriptionEndpointContainsDynamicFilter() {
            String selector = "a=b";
            String exchangeName = "subscription-exchange";
            String dynamicFilter = "originatingCountry='NO'";
            Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED);
            subscription.setEndpoints(singleton(new Endpoint("my-source", "my-host", 5671, dynamicFilter)));
            subscription.setConsumerCommonName("my-node");

            client.createHeadersExchange(exchangeName);

            Neighbour myNeighbour = new Neighbour("neighbour",new NeighbourCapabilities(),new NeighbourSubscriptionRequest(),new SubscriptionRequest(singleton(subscription)));
			neighbourRepository.save(myNeighbour);

            routingConfigurer.setUpSubscriptionExchanges();

            assertThat(subscription.getEndpoints()).hasSize(1);
        }

        @Test
        public void neighbourWithShardAndExchangeButNoListenerEndpointGetsListenerEndpointCreated() {
            String exchangeName = "my-test-exchange";
            String name = "neighbour";
            String source = "neighbour-endpoint";
            Neighbour neighbour = new Neighbour(
                    name,
                    new NeighbourCapabilities(),
                    new NeighbourSubscriptionRequest(),
                    new SubscriptionRequest(
                            Set.of(
                                    new Subscription(
                                            SubscriptionStatus.CREATED,
                                            "a = b",
                                            "/subscriptions/1",
                                            interchangeNodeProperties.getName(),
                                            Set.of(
                                                    new Endpoint(
                                                            source,
                                                            name,
                                                            1234,
                                                            new SubscriptionShard(
                                                                    exchangeName
                                                            )
                                                    )
                                            )

                                    )
                            )
                    )
            );
            client.createHeadersExchange(exchangeName);
            neighbourRepository.save(neighbour);
            routingConfigurer.setUpSubscriptionExchanges();
            assertThat(
                    listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName(exchangeName,source,name)
            ).isNotNull();

        }


        @Test
        public void tearDownSubscriptionShardExchange() {
            String selector = "a=b";
            String exchangeName = "subscription-exchange-teardown-shard";
            Subscription subscription = new Subscription(selector, SubscriptionStatus.TEAR_DOWN);
            subscription.setEndpoints(singleton(new Endpoint("my-source", "my-host", 5671, new SubscriptionShard(exchangeName))));
            subscription.setConsumerCommonName(interchangeNodeProperties.getName());

            client.createHeadersExchange(exchangeName);

            Neighbour myNeighbour = new Neighbour("neighbour",new NeighbourCapabilities(),new NeighbourSubscriptionRequest(),new SubscriptionRequest(singleton(subscription)));
			neighbourRepository.save(myNeighbour);

            routingConfigurer.tearDownSubscriptionExchanges();
            assertThat(subscription.getEndpoints().isEmpty()).isTrue();
            assertThat(client.exchangeExists(exchangeName)).isFalse();
        }

        @Test
        public void multipleEndpointsAndSubscriptionShardExchangesAreRemoved() {
            String selector = "originatingCountry = 'NO'";
            Subscription subscription = new Subscription(selector, SubscriptionStatus.TEAR_DOWN);

            Endpoint end1 = new Endpoint(
                    "my-source-1",
                    "my-host",
                    5671,
                    new SubscriptionShard("exchange1")
            );

            Endpoint end2 = new Endpoint(
                    "my-source-2",
                    "my-host",
                    5671,
                    new SubscriptionShard("exchange2")
            );

            subscription.setEndpoints(new HashSet<>(List.of(end1, end2)));
            subscription.setConsumerCommonName(interchangeNodeProperties.getName());

            client.createHeadersExchange("exchange1");
            client.createHeadersExchange("exchange2");

            Neighbour myNeighbour = new Neighbour("Neighbour",new NeighbourCapabilities(),new NeighbourSubscriptionRequest(),new SubscriptionRequest(singleton(subscription)));
			neighbourRepository.save(myNeighbour);

            routingConfigurer.tearDownSubscriptionExchanges();
            assertThat(subscription.getEndpoints().isEmpty()).isTrue();
            assertThat(client.exchangeExists("exchange1")).isFalse();
            assertThat(client.exchangeExists("exchange2")).isFalse();
        }


        @Test
        public void redirectSubscriptionHasItsEndpointRemovedWhenItIsTearDown() {
            String selector = "originatingCountry = 'NO'";
            Subscription subscription = new Subscription(
                    SubscriptionStatus.TEAR_DOWN,
                    selector,
                    "/path",
                    "sp1",
                    Set.of(
                            new Endpoint(
                                    "source",
                                    "otherhost",
                                    5671
                            )
                    )
            );
            Neighbour neighbour = new Neighbour(
                    "neighbour1",
                    new NeighbourCapabilities(),
                    new NeighbourSubscriptionRequest(),
                    new SubscriptionRequest(
                        Set.of(
                                subscription
                        )
                    )
            );
			neighbourRepository.save(neighbour);
            routingConfigurer.tearDownSubscriptionExchanges();
            assertThat(subscription.getEndpoints()).isEmpty();

        }

        @Test
        public void subscriptionExchangeAndSubscriptionShardIsRemovedWhenSubscriptionHasStatusFailed() {
            String exchangeName = "failed-exchange";
            Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED);
            Endpoint endpoint = new Endpoint("my-source", "my-host", 5671, new SubscriptionShard(exchangeName));
            subscription.setEndpoints(singleton(endpoint));
            subscription.setConsumerCommonName(interchangeNodeProperties.getName());

            client.createHeadersExchange(exchangeName);

            Neighbour myNeighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(singleton(subscription)));
			neighbourRepository.save(myNeighbour);

            routingConfigurer.tearDownSubscriptionExchanges();
            assertThat(subscription.getEndpoints().isEmpty()).isFalse();
            assertThat(client.exchangeExists(exchangeName)).isFalse();
            assertThat(endpoint.hasShard()).isFalse();
        }

        @Test
        public void subscriptionExchangeAndSubscriptionShardIsNotRemovedWhenSubscriptionHasStatusFailedAndListenerEndpointExists() {
            String exchangeName = "failed-exchange-with-listener-endpoint";
            Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED);
            Endpoint endpoint = new Endpoint("my-source", "my-host", 5671, new SubscriptionShard(exchangeName));
            subscription.setEndpoints(singleton(endpoint));
            subscription.setConsumerCommonName("my-node");

            client.createHeadersExchange(exchangeName);

            Neighbour myNeighbour = new Neighbour("my-neighbour",
                    new NeighbourCapabilities(),
                    new NeighbourSubscriptionRequest(),
                    new SubscriptionRequest(singleton(subscription)));
			neighbourRepository.save(myNeighbour);

            routingConfigurer.tearDownSubscriptionExchanges();
            assertThat(subscription.getEndpoints().isEmpty()).isFalse();
            assertThat(client.exchangeExists(exchangeName)).isTrue();
            assertThat(endpoint.hasShard()).isTrue();
        }

        @Test
        @Disabled
        public void setupRegularRoutingWithNonExistingExchangeKeepsTheSubscriptionUnchanged() {
            Capability denmCapability = new Capability(
                    new DenmApplication(
                            "NO0000",
                            "NO0000:001",
                            "NO",
                            "1.0",
                            List.of("0122"),
                            List.of(1)
                    ),
                    new Metadata(RedirectStatus.NOT_AVAILABLE)
            );
            NeighbourSubscription neighbourSubscription = new NeighbourSubscription(
                    "publisherId = 'NO0000'",
                    NeighbourSubscriptionStatus.ACCEPTED,
                    "my_Neighbour"
            );
			Neighbour neighbour = new Neighbour(
				"neighbour",
				new NeighbourCapabilities(),
				new NeighbourSubscriptionRequest(Set.of(neighbourSubscription)),
				new SubscriptionRequest()
			);
			neighbourRepository.save(neighbour);

            routingConfigurer.setUpRegularRouting(
                    Set.of(neighbourSubscription),
                    Set.of(denmCapability),
                    "my_Neighbour",
                    client.getQpidDelta()
            );
            assertThat(neighbourSubscription.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.ACCEPTED);
            assertThat(neighbourSubscription.getEndpoints()).isEmpty();
        }

        @Test
        public void tearDownSubscriptionShouldRemoveAclForQueue() {
            String neighbourName = "neighbour-tear-down";
            String queueName = UUID.randomUUID().toString();
            NeighbourEndpoint endpoint = new NeighbourEndpoint(
                    queueName,
                    "hostName",
                    5671
            );
            NeighbourSubscription subscription = new NeighbourSubscription(
                    "a = b",
                    NeighbourSubscriptionStatus.TEAR_DOWN,
                    neighbourName
            );
            subscription.setEndpoints(singleton(endpoint));
            Neighbour neighbour = new Neighbour(
                    neighbourName,
                    new NeighbourCapabilities(),
                    new NeighbourSubscriptionRequest(
                            singleton(subscription)
                    ),
                    new SubscriptionRequest()
            );
			neighbourRepository.save(neighbour);
            Queue queue = client.createQueue(queueName);
            client.addNeighbourMemberToGroup(neighbourName);
            client.addReadAccess(neighbourName,queue.getName());
            routingConfigurer.tearDownNeighbourRouting(neighbour);
            assertThat(client.getNeighbourMember(neighbourName)).isNull();
            assertThat(client
                    .getQpidAcl()
                    .containsRule(VirtualHostAccessController
                            .createQueueReadAccessRule(neighbourName,queue.getName())
                    )
            ).isFalse();
            assertThat(client.getQueue(queue.getName())).isNull();
        }

        @Test
        public void tearDownSubscriptionShouldNotRemoveNeighbourFromGroupIfOtherSubsExist() {
            String neighbourName = "non-redirect-neighbour";
            String queueName = UUID.randomUUID().toString();
            NeighbourEndpoint endpoint = new NeighbourEndpoint(
                    queueName,
                    neighbourName,
                    5671
            );
            NeighbourSubscription subscription = new NeighbourSubscription(
					UUID.randomUUID().toString(),
                    NeighbourSubscriptionStatus.TEAR_DOWN,
					"a = b",
					"/subs1",
					neighbourName,
					Set.of(endpoint)
			);
            String nonTeardownQueueName = "non-teardown-queue";
            NeighbourEndpoint nonTearDownEndpoint = new NeighbourEndpoint(
                    nonTeardownQueueName,
                    neighbourName,
                    5671
            );
            NeighbourSubscription nonTearDownSubscription = new NeighbourSubscription(
					UUID.randomUUID().toString(),
                    NeighbourSubscriptionStatus.CREATED,
					"c = d",
					"/subs1",
					neighbourName,
					Set.of(nonTearDownEndpoint)
			);
            Neighbour neighbour = new Neighbour(
                    neighbourName,
                    new NeighbourCapabilities(),
                    new NeighbourSubscriptionRequest(
                            Set.of(subscription,nonTearDownSubscription)
                    ),
                    new SubscriptionRequest()
            );
			neighbourRepository.save(neighbour);
            Queue queue = client.createQueue(queueName);
            Queue nonTeardownQueue = client.createQueue(nonTeardownQueueName);
            client.addNeighbourMemberToGroup(neighbourName);
            client.addReadAccess(neighbourName,queue.getName());
            client.addReadAccess(neighbourName,nonTeardownQueue.getName());

            routingConfigurer.tearDownNeighbourRouting(neighbour);


            assertThat(client.getNeighbourMember(neighbourName)).isNotNull();
            VirtualHostAccessController qpidAcl = client.getQpidAcl();
            assertThat(qpidAcl
                    .containsRule(VirtualHostAccessController
                            .createQueueReadAccessRule(neighbourName,queue.getName())
                    )
            ).isFalse();
            assertThat(qpidAcl
                    .containsRule(VirtualHostAccessController
                            .createQueueReadAccessRule(neighbourName,nonTeardownQueue.getName())
                    )
            ).isTrue();
            assertThat(client.getQueue(queue.getName())).isNull();
            assertThat(client.getQueue(nonTeardownQueue.getName())).isNotNull();
        }

        @Test
        public void tearDownRedirectedSubscriptionShouldRemoveAclForQueue() {
            String neighbourSPName = "neighbour-service-provider";
            String queueName = UUID.randomUUID().toString();
            NeighbourEndpoint endpoint = new NeighbourEndpoint(
                    queueName,
                    "hostName",
                    5671
            );
            NeighbourSubscription subscription = new NeighbourSubscription(
					UUID.randomUUID().toString(),
					NeighbourSubscriptionStatus.TEAR_DOWN,
					"a = b",
					"/subs1",
					neighbourSPName,
					Set.of(endpoint)
			);
            String neighbourName = "redirect-neighbour-acl-test";
            Neighbour neighbour = new Neighbour(
                    neighbourName,
                    new NeighbourCapabilities(),
                    new NeighbourSubscriptionRequest(
                            singleton(subscription)
                    ),
                    new SubscriptionRequest()
            );
			neighbourRepository.save(neighbour);
            Queue queue = client.createQueue(queueName);
            client.addNeighbourMemberToGroup(neighbourName);
            client.addReadAccess(neighbourSPName,queue.getName());
            client.addRemoteServiceProvicerMemberToGroup(neighbourSPName);
            routingConfigurer.tearDownNeighbourRouting(neighbour);
            assertThat(client.getNeighbourMember(neighbourName)).isNull();
            assertThat(client
                    .getQpidAcl()
                    .containsRule(VirtualHostAccessController
                            .createQueueReadAccessRule(neighbourSPName,queue.getName())
                    )
            ).isFalse();
            assertThat(client.getRemoteServiceProviderMember(neighbourSPName)).isNull();
            assertThat(client.getQueue(queue.getName())).isNull();
        }

        @Test
        public void tearDownRedirectedSubscriptionShouldNotRemoveSPFromGroupIfOtherRedirectSubsExist() {
            String neighbourSPName = "neighbour-non-teardown-service-provider-1";
            String queueName = UUID.randomUUID().toString();
            NeighbourEndpoint endpoint = new NeighbourEndpoint(
                    queueName,
                    "hostName",
                    5671
            );
            NeighbourSubscription subscription = new NeighbourSubscription(
					UUID.randomUUID().toString(),
					NeighbourSubscriptionStatus.TEAR_DOWN,
					"a = b",
					"/subs1",
                    neighbourSPName,
					Set.of(endpoint)
			);
            String nonTeardownQueueName = "non-teardown-redirected-queue";
            NeighbourEndpoint endpointNonTearDown = new NeighbourEndpoint(
                    nonTeardownQueueName,
                    "hostName",
                    5671
            );
            NeighbourSubscription nonTearDownSubscription = new NeighbourSubscription(
					UUID.randomUUID().toString(),
					NeighbourSubscriptionStatus.CREATED,
					"c = d",
					"/subs2",
                    neighbourSPName,
					Set.of(endpointNonTearDown)
			);
            String neighbourName = "redirect-neighbour";
            Neighbour neighbour = new Neighbour(
                    neighbourName,
                    new NeighbourCapabilities(),
                    new NeighbourSubscriptionRequest(
							Set.of (subscription,nonTearDownSubscription)
                    ),
                    new SubscriptionRequest()
            );
			neighbourRepository.save(neighbour);
            Queue queue = client.createQueue(queueName);
            Queue nonTeardownQueue = client.createQueue(nonTeardownQueueName);
            client.addNeighbourMemberToGroup(neighbourName);
            client.addReadAccess(neighbourSPName,queue.getName());
            client.addReadAccess(neighbourSPName,nonTeardownQueue.getName());
            client.addRemoteServiceProvicerMemberToGroup(neighbourSPName);

            routingConfigurer.tearDownNeighbourRouting(neighbour);


            assertThat(client.getNeighbourMember(neighbourName)).isNotNull();
            VirtualHostAccessController qpidAcl = client.getQpidAcl();
            assertThat(qpidAcl
                    .containsRule(VirtualHostAccessController
                            .createQueueReadAccessRule(neighbourSPName,queue.getName())
                    )
            ).isFalse();
            assertThat(qpidAcl
                    .containsRule(VirtualHostAccessController
                            .createQueueReadAccessRule(neighbourSPName,nonTeardownQueue.getName())
                    )
            ).isTrue();
            assertThat(client.getRemoteServiceProviderMember(neighbourSPName)).isNotNull();
            assertThat(client.getQueue(queue.getName())).isNull();
            assertThat(client.getQueue(nonTeardownQueue.getName())).isNotNull();
        }

        @Test
        public void tearDownRedirectedSubscriptionShouldNotAffectOtherRedirectedSubscriptions() {
            String neighbourSPName = "neighbour-service-provider-teardown-x";
            String otherNeighbourSPName = "other-neighbour-service-provider";
            String queueName = UUID.randomUUID().toString();
            NeighbourEndpoint endpoint = new NeighbourEndpoint(
                    queueName,
                    "hostName",
                    5671
            );
            NeighbourSubscription subscription = new NeighbourSubscription(
					UUID.randomUUID().toString(),
					NeighbourSubscriptionStatus.TEAR_DOWN,
					"a = b",
					"/subs1",
                    neighbourSPName,
					Set.of(endpoint)
			);
            String nonTeardownQueueName = "non-teardown-redirected-queue-x";
            NeighbourEndpoint endpointNonTearDown = new NeighbourEndpoint(
                    nonTeardownQueueName,
                    "hostName",
                    5671
            );
            NeighbourSubscription nonTearDownSubscription = new NeighbourSubscription(
					UUID.randomUUID().toString(),
					NeighbourSubscriptionStatus.CREATED,
					"c = d",
					"/subs2",
                    otherNeighbourSPName,
					Set.of(endpointNonTearDown)
			);
            String neighbourName = "redirect-neighbour-xx";
            Neighbour neighbour = new Neighbour(
                    neighbourName,
                    new NeighbourCapabilities(),
                    new NeighbourSubscriptionRequest(
                            new HashSet<>(Arrays.asList(subscription,nonTearDownSubscription))
                    ),
                    new SubscriptionRequest()
            );
			neighbourRepository.save(neighbour);
            Queue queue = client.createQueue(queueName);
            Queue nonTeardownQueue = client.createQueue(nonTeardownQueueName);
            client.addNeighbourMemberToGroup(neighbourName);
            client.addReadAccess(neighbourSPName,queue.getName());
            client.addReadAccess(otherNeighbourSPName,nonTeardownQueue.getName());
            client.addRemoteServiceProvicerMemberToGroup(neighbourSPName);
            client.addRemoteServiceProvicerMemberToGroup(otherNeighbourSPName);

            routingConfigurer.tearDownNeighbourRouting(neighbour);


            assertThat(client.getNeighbourMember(neighbourName)).isNotNull();
            VirtualHostAccessController qpidAcl = client.getQpidAcl();
            assertThat(qpidAcl
                    .containsRule(VirtualHostAccessController
                            .createQueueReadAccessRule(neighbourSPName,queue.getName())
                    )
            ).isFalse();
            assertThat(qpidAcl
                    .containsRule(VirtualHostAccessController
                            .createQueueReadAccessRule(otherNeighbourSPName,nonTeardownQueue.getName())
                    )
            ).isTrue();
            assertThat(client.getRemoteServiceProviderMember(neighbourSPName)).isNull();
            assertThat(client.getRemoteServiceProviderMember(otherNeighbourSPName)).isNotNull();
            assertThat(client.getQueue(queue.getName())).isNull();
            assertThat(client.getQueue(nonTeardownQueue.getName())).isNotNull();
        }

        @Test
        public void teardownNeighbourSubscriptionsThatDoesNotExistInBroker() {
            String uuid = UUID.randomUUID().toString();
            String neighbourName = "neighbour";
            Neighbour neighbour = new Neighbour(
                    neighbourName,
                    new NeighbourCapabilities(),
                    new NeighbourSubscriptionRequest(
                           Set.of(
                                   new NeighbourSubscription(
                                           uuid,
                                           NeighbourSubscriptionStatus.TEAR_DOWN,
                                           "a = b",
                                           "/subs/" + uuid,
                                            neighbourName,
                                           Set.of(
                                                   new NeighbourEndpoint(
                                                           UUID.randomUUID().toString(),
                                                           "broker." + neighbourName,
                                                           443
                                                   )
                                           )
                                   )
                           )
                   ),
                   new SubscriptionRequest()
            );
			neighbourRepository.save(neighbour);
            routingConfigurer.tearDownNeighbourRouting(neighbour);
        }

        @Test
        public void oneShardedCapabilityAndOneShardedRedirectSubscription() {
            Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex30", "cap-ex31", "cap-ex32");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex30");
            client.createHeadersExchange("cap-ex31");
            client.createHeadersExchange("cap-ex32");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-1");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
            assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(client.getRemoteServiceProviderMember("redirect-sp-1")).isNotNull();
        }

        @Test
        public void oneShardedCapabilityAndTwoShardedRedirectSubscriptions() {
            Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex33", "cap-ex34", "cap-ex35");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex33");
            client.createHeadersExchange("cap-ex34");
            client.createHeadersExchange("cap-ex35");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub1 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-2");
            NeighbourSubscription sub2 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-3");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(new HashSet<>(Arrays.asList(sub1, sub2))), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub1.getEndpoints()).hasSize(1);
            assertThat(sub2.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub1.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
            assertThat(client.queueExists(sub2.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub2.getEndpoints().stream().findFirst().get().getSource())).hasSize(2);
            assertThat(sub1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(sub2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(client.getRemoteServiceProviderMember("redirect-sp-2")).isNotNull();
            assertThat(client.getRemoteServiceProviderMember("redirect-sp-3")).isNotNull();
        }

        @Test
        public void oneShardedCapabilityAndNotShardedRedirectSubscription() {
            Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex36", "cap-ex37", "cap-ex38");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex36");
            client.createHeadersExchange("cap-ex37");
            client.createHeadersExchange("cap-ex38");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-4");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub.getEndpoints()).hasSize(1);
            assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().get().getSource())).hasSize(3);
            assertThat(client.getRemoteServiceProviderMember("redirect-sp-4")).isNotNull();
        }

        @Test
        public void oneCapabilityNotShardedAndOneRedirectSubscriptionSharded() {
            Capability cap = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex39");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex39");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-5");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub.getEndpoints()).hasSize(0);
            assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.NO_OVERLAP);
            assertThat(client.getRemoteServiceProviderMember("redirect-sp-4")).isNull();
        }

        @Test
        public void oneShardedCapabilityAndOneRedirectSubscriptionShardedAndOneRedirectSubscriptionNotSharded() {
            Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex40", "cap-ex41", "cap-ex42");
            cap.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex40");
            client.createHeadersExchange("cap-ex41");
            client.createHeadersExchange("cap-ex42");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub1 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-6");
            NeighbourSubscription sub2 = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-7");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(new HashSet<>(Arrays.asList(sub1, sub2))), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub1.getEndpoints()).hasSize(1);
            assertThat(sub2.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub1.getEndpoints().stream().findFirst().get().getSource())).hasSize(2);
            assertThat(client.queueExists(sub2.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub2.getEndpoints().stream().findFirst().get().getSource())).hasSize(3);
            assertThat(sub1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(sub2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(client.getRemoteServiceProviderMember("redirect-sp-6")).isNotNull();
            assertThat(client.getRemoteServiceProviderMember("redirect-sp-7")).isNotNull();
        }

        @Test
        public void twoCapabilitiesShardedAndOneRedirectSubscriptionSharded() {
            Capability cap1 = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex43", "cap-ex44", "cap-ex45");
            cap1.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex43");
            client.createHeadersExchange("cap-ex44");
            client.createHeadersExchange("cap-ex45");

            Capability cap2 = getShardedCapability("pub-2", RedirectStatus.OPTIONAL, "cap-ex46", "cap-ex47", "cap-ex48");
            cap2.setStatus(CapabilityStatus.CREATED);
            client.createHeadersExchange("cap-ex46");
            client.createHeadersExchange("cap-ex47");
            client.createHeadersExchange("cap-ex48");

            ServiceProvider sp = new ServiceProvider("sp",new Capabilities(Set.of(cap1, cap2)));
			serviceProviderRepository.save(sp);

            NeighbourSubscription sub = new NeighbourSubscription("(publicationId = 'pub-1' OR publicationId = 'pub-2') AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-8");

            Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());
			neighbourRepository.save(neighbour);

            routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

            assertThat(sub.getEndpoints()).hasSize(1);
            assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
            assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().get().getSource())).hasSize(4);
            assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
            assertThat(client.getRemoteServiceProviderMember("redirect-sp-8")).isNotNull();
        }

	public Capability getDatexCapability(String publicationId, RedirectStatus redirect, String exchangeName) {
		CapabilityShard shard = new CapabilityShard(1, exchangeName, "publicationId = '" + publicationId + "'");
		Capability cap = new Capability(
				new DatexApplication(
						"NO-1234",
						publicationId,
						"NO",
						"1.0",
						Arrays.asList("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				new Metadata(redirect),
				Collections.singletonList(shard)
		);
		return cap;
	}

	public Capability getShardedCapability(String publicationId, RedirectStatus redirect, String exchangeName1, String exchangeName2, String exchangeName3) {
		Metadata metadata = new Metadata(redirect);
		CapabilityShard shard1 = new CapabilityShard(1, exchangeName1, "publicationId = '" + publicationId + "'");
		CapabilityShard shard2 = new CapabilityShard(2, exchangeName2, "publicationId = '" + publicationId + "'");
		CapabilityShard shard3 = new CapabilityShard(3, exchangeName3, "publicationId = '" + publicationId + "'");
		Capability cap = new Capability(
				new DatexApplication(
						"NO-1234",
						publicationId,
						"NO",
						"1.0",
						Arrays.asList("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata,
				List.of(shard1, shard2, shard3)
		);
		return cap;
	}

}
