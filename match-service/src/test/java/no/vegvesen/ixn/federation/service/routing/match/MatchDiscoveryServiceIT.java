package no.vegvesen.ixn.federation.service.routing.match;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@Transactional
public class MatchDiscoveryServiceIT  { //extends PostgresContainerBase {


    public static final String HOST_NAME = QpidDockerBaseIT.getDockerHost();
    private static final ClusterKeyGenerator.CaStores stores = QpidDockerBaseIT.generateStores(QpidDockerBaseIT.getTargetFolderPathForTestClass(MatchDiscoveryServiceIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf");

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18.1")
            .withDatabaseName("federation")
            .withUsername("federation")
            .withPassword("federation");

    @Container
    public static QpidContainer qpidContainer = QpidDockerBaseIT.getQpidTestContainer(
            stores,
            HOST_NAME,
            HOST_NAME,
            Path.of("qpid")
    ).dependsOn(postgreSQLContainer);


    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
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
        registry.add("spring.ssl.bundle.jks.qpid-client.keystore.location", () -> routingConfigurerStore.path().toString());
        registry.add("spring.ssl.bundle.jks.qpid-client.keystore.password", routingConfigurerStore::password);
        registry.add("spring.ssl.bundle.jks.qpid-client.truststore.location", () -> caStore.truststoreName().toString());
        registry.add("spring.ssl.bundle.jks.qpid-client.truststore.password", caStore::truststorePassword);
    }

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private NeighbourRepository neighbourRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private RoutingConfigurerProperties routingConfigurerProperties;

    @Autowired
    private QpidClient client;

    @Autowired
    private MatchDiscoveryService matchDiscoveryService;

    @Test
    public void matchDiscovereryServiceIsAutowired() {
        assertThat(matchDiscoveryService).isNotNull();
    }

    @Test
    public void repositoriesAreAutowired() {
        assertThat(matchRepository).isNotNull();
        assertThat(serviceProviderRepository).isNotNull();
        assertThat(neighbourRepository).isNotNull();
    }

    @Test
    public void twoLocalSubscriptionsCanHaveMatchToTheSameSubscription() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        LocalSubscription localSubscription1 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
        LocalSubscription localSubscription2 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider1 = new ServiceProvider("service-provider1", Set.of(localSubscription1));
        serviceProviderRepository.save(serviceProvider1);

        ServiceProvider serviceProvider2 = new ServiceProvider("service-provider2",Set.of(localSubscription2));
        serviceProviderRepository.save(serviceProvider2);

        Subscription subscription = new Subscription(SubscriptionStatus.CREATED, selector, "", consumerCommonName);
        Neighbour neighbour = new Neighbour("neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(
                        Collections.singleton(subscription)
                )
        );
        neighbourRepository.save(neighbour);

        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Arrays.asList(serviceProvider1, serviceProvider2), Collections.singletonList(neighbour));
        assertThat(matchRepository.findAll()).hasSize(2);
    }

    @Test
    public void setupRedirectMatch() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "service-provider";
        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider = new ServiceProvider("service-provider",Set.of(localSubscription));
        serviceProviderRepository.save(serviceProvider);

        Subscription subscription = new Subscription(SubscriptionStatus.CREATED, selector, "", consumerCommonName);
        Neighbour neighbour = new Neighbour("neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(
                        Collections.singleton(subscription)
                )
        );
        neighbourRepository.save(neighbour);
        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.singletonList(serviceProvider), Collections.singletonList(neighbour));

        assertThat(matchRepository.findAll()).hasSize(1);
    }

    @Test
    public void twoLocalSubscriptionsCanHaveMatchToTheSameSubscriptionWhenOneIsAlreadySetUp() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        LocalSubscription localSubscription1 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider1 = new ServiceProvider("service-provider1",Set.of(localSubscription1));
        serviceProviderRepository.save(serviceProvider1);

        Subscription subscription = new Subscription(SubscriptionStatus.CREATED, selector, "", consumerCommonName);
        Neighbour neighbour = new Neighbour("neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(
                        Collections.singleton(subscription)
                )
        );
        neighbourRepository.save(neighbour);

        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.singletonList(serviceProvider1), Collections.singletonList(neighbour));
        assertThat(matchRepository.findAll()).hasSize(1);

        Neighbour savedNeighbour = neighbourRepository.findByName(neighbour.getName());

        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream()
                .findFirst()
                .get();

        assertThat(matchRepository.findAllBySubscriptionId(savedSubscription.getId())).hasSize(1);

        LocalSubscription localSubscription2 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider2 = new ServiceProvider("service-provider2",Set.of(localSubscription2));
        serviceProviderRepository.save(serviceProvider2);

        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Arrays.asList(serviceProvider1, serviceProvider2), Collections.singletonList(neighbour));

        assertThat(matchRepository.findAll()).hasSize(2);
        assertThat(matchRepository.findAllBySubscriptionId(savedSubscription.getId())).hasSize(2);
    }

    @Test
    public void matchIsNotSetUpWhenSubscriptionIsInTearDown() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider = new ServiceProvider("service-provider",Set.of(localSubscription));
        serviceProviderRepository.save(serviceProvider);

        Subscription subscription = new Subscription(SubscriptionStatus.TEAR_DOWN, selector, "", consumerCommonName);
        Neighbour neighbour = new Neighbour("neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(
                        Collections.singleton(subscription)
                )
        );
        neighbourRepository.save(neighbour);

        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.singletonList(serviceProvider), Collections.singletonList(neighbour));
        assertThat(matchRepository.findAll()).hasSize(0);
    }

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreated() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue";
		String exchangeName = "sub-exchange";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);


		LocalSubscription localSubscription = new LocalSubscription(UUID.randomUUID().toString(), LocalSubscriptionStatus.CREATED, selector, consumerCommonName, new HashSet<>(), Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider",Set.of(localSubscription));

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);

		Endpoint endpoint = new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName));
		subscription.setEndpoints(Collections.singleton(endpoint));

		Match match = new Match(localSubscription, subscription);

        serviceProviderRepository.save(serviceProvider);
        matchRepository.save(match);
        neighbourRepository.save(new Neighbour("neighbour",new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Set.of(subscription))));
		//when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		//when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.singletonList(match));
        matchDiscoveryService.createBindingsWithMatches();

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(1);
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
	}

    @Test
    public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionsCreated() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        String queueName = "loc-sub-queue-1";
        String exchangeName = "sub-exchange-1";
        String exchangeName2 = "sub-exchange-2";

        client.createQueue(queueName);
        client.createHeadersExchange(exchangeName);
        client.createHeadersExchange(exchangeName2);


        LocalSubscription localSubscription = new LocalSubscription(
                UUID.randomUUID().toString(),
                LocalSubscriptionStatus.CREATED,
                selector,
                consumerCommonName,
                new HashSet<>(),
                Set.of(
                        new LocalEndpoint(
                                queueName,
                                "my-node",
                                5671
                        )
                )
        );
        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider", Set.of(localSubscription));
        serviceProviderRepository.save(serviceProvider);

        Subscription subscription = new Subscription(
                SubscriptionStatus.CREATED,
                selector,
                "/neighoubour-1/aa",
                consumerCommonName,
                Set.of(
                        new Endpoint(
                                "source",
                                "host",
                                5671,
                                new SubscriptionShard(exchangeName)
                        )
                )
        );

        Neighbour neighbour = new Neighbour("neighbour-1",new NeighbourCapabilities(),new NeighbourSubscriptionRequest(), new SubscriptionRequest(Set.of(subscription)));
        neighbourRepository.save(neighbour);
        Subscription subscription2 = new Subscription(
                SubscriptionStatus.CREATED,
                selector,
                "neighbour-1/bb",
                consumerCommonName,
                Set.of(
                        new Endpoint("source2",
                                "host",
                                 5671,
                                  new SubscriptionShard(exchangeName2)
                        )
                )
        );
        Neighbour neighbour2 = new Neighbour("neighbour-2", new NeighbourCapabilities(),new  NeighbourSubscriptionRequest(), new SubscriptionRequest(Set.of(subscription2)));
        neighbourRepository.save(neighbour2);

        Match match = new Match(localSubscription, subscription);
        Match match2 = new Match(localSubscription, subscription2);
        matchRepository.save(match);
        matchRepository.save(match2);

        matchDiscoveryService.createBindingsWithMatches();

        assertThat(client.getQueuePublishingLinks(queueName)).hasSize(2);
        assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
        assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName2));
    }


    @Test
    public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreatedBindKeyAlreadyExists() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        String queueName = "loc-sub-queue-4";
        String exchangeName = "sub-exchange-4";

        client.createQueue(queueName);
        client.createHeadersExchange(exchangeName);


        LocalSubscription localSubscription = new LocalSubscription(UUID.randomUUID().toString(), LocalSubscriptionStatus.CREATED, selector, consumerCommonName, new HashSet<>(), Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider",Set.of(localSubscription));
        serviceProviderRepository.save(serviceProvider);


        Subscription subscription = new Subscription(SubscriptionStatus.CREATED,selector,"/neighbour-1/sub1",consumerCommonName,Set.of(new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName))));
        Neighbour neighbour = new Neighbour("neighbour-1",new NeighbourCapabilities(),new NeighbourSubscriptionRequest(), new SubscriptionRequest(Set.of(subscription)));
        neighbourRepository.save(neighbour);

        Match match = new Match(localSubscription, subscription);
        matchRepository.save(match);

        //Mocking that binding already exists and isn't created again
        client.addBinding(exchangeName, new Binding(exchangeName, queueName, new Filter(selector)));

        matchDiscoveryService.createBindingsWithMatches();

        assertThat(client.getQueuePublishingLinks(queueName)).hasSize(1);
        assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
    }

    @Test
    public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionTearDown() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        String queueName = "loc-sub-queue-3";
        String exchangeName = "sub-exchange-3";

        client.createQueue(queueName);
        client.createHeadersExchange(exchangeName);


        LocalSubscription localSubscription = new LocalSubscription(UUID.randomUUID().toString(), LocalSubscriptionStatus.CREATED, selector, consumerCommonName, new HashSet<>(), Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider",Set.of(localSubscription));
        serviceProviderRepository.save(serviceProvider);


        Subscription subscription = new Subscription(SubscriptionStatus.TEAR_DOWN, selector,"/neigbhour-1/sub1",consumerCommonName,Set.of(new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName))));
        Neighbour neighbour = new Neighbour("neigbour-1",new NeighbourCapabilities(),new NeighbourSubscriptionRequest(), new SubscriptionRequest(Set.of(subscription)));
        neighbourRepository.save(neighbour);

        Match match = new Match(localSubscription, subscription);
        matchRepository.save(match);

        matchDiscoveryService.createBindingsWithMatches();

        assertThat(client.getQueuePublishingLinks(queueName)).hasSize(0);
        assertThat(client.getQueuePublishingLinks(queueName)).noneMatch(b -> b.getBindingKey().equals(exchangeName));
    }

    @Test
    public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreatedButNoMatchYet() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        String queueName = "loc-sub-queue-5";
        String exchangeName = "sub-exchange-5";

        client.createQueue(queueName);
        client.createHeadersExchange(exchangeName);


        LocalSubscription localSubscription = new LocalSubscription(
                UUID.randomUUID().toString(),
                LocalSubscriptionStatus.CREATED, selector, consumerCommonName,
                Set.of(),
                Set.of(new LocalEndpoint(queueName, "my-node", 5671)));

        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider",Set.of(localSubscription));
        serviceProviderRepository.save(serviceProvider);


        Subscription subscription = new Subscription(SubscriptionStatus.CREATED, selector,"/neighbour-1/a",consumerCommonName,Set.of(new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName))));
        Neighbour neighbour = new Neighbour("neighour-1",new NeighbourCapabilities(),new  NeighbourSubscriptionRequest(), new SubscriptionRequest(Set.of(subscription)));
        neighbourRepository.save(neighbour);

        Match match = new Match(localSubscription, subscription);
        matchRepository.save(match);

        matchDiscoveryService.createBindingsWithMatches();

        assertThat(client.getQueuePublishingLinks(queueName)).hasSize(1);
        assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
    }

    @Test
    public void createBindingsWithMatchesWhereSubscriptionExchangeIsNotAlreadyCreated() {
        String name = "service-provider-no-subs-exchange-setup";
        String source = "no-subs-exchange-setup-local-sub";
        client.createQueue(source);
        LocalSubscription localSubscription = new LocalSubscription(
                LocalSubscriptionStatus.REQUESTED,
                "a = b",
                qpidContainer.getvHostName(),
                Set.of(),
                Set.of(
                        new LocalEndpoint(
                                source,
                                qpidContainer.getHost(),
                                qpidContainer.getAmqpsPort()
                        )
                )
        );
        ServiceProvider serviceProvider = new ServiceProvider(
                name,
                Set.of(
                        localSubscription
                )
        );
        serviceProviderRepository.save(serviceProvider);

        String exchangeName = "this-is-my-non-existing-local-exchange";
        Subscription subscription = new Subscription(
                SubscriptionStatus.REQUESTED,
                "a = b",
                "",
                "a=b",
                Set.of(new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName)))
        );
        Neighbour neighbour = new Neighbour("neighoubr-1",new NeighbourCapabilities(),new NeighbourSubscriptionRequest(), new SubscriptionRequest(Set.of(subscription)));
        neighbourRepository.save(neighbour);

        Match match = new Match(
                localSubscription,
                subscription
        );
        matchRepository.save(match);
        matchDiscoveryService.createBindingsWithMatches();

        assertThat(client.exchangeExists(exchangeName)).isFalse();
        assertThat(client.getQueuePublishingLinks(source)).doesNotContain(new Binding(source, name, new Filter("a = b")));
    }

    @Test
    public void createBindingsWithMatchesWhereLocalSubscriptionQueueIsNotAlreadyCreated() {
        String name = "service-provider-no-local-subs-queue-setup";
        String source = "no-local-subs-queue-setup-local-sub";
        String exchangeName = "this-is-my-existing-local-exchange";
        client.createHeadersExchange(exchangeName);
        LocalSubscription localSubscription = new LocalSubscription(
                LocalSubscriptionStatus.REQUESTED,
                "a = b",
                qpidContainer.getvHostName(),
                Set.of(),
                Set.of(
                        new LocalEndpoint(
                                source,
                                qpidContainer.getHost(),
                                qpidContainer.getAmqpsPort()
                        )
                )
        );
        ServiceProvider serviceProvider = new ServiceProvider(
                name,
                Set.of(
                        localSubscription
                )
        );
        serviceProviderRepository.save(serviceProvider);
        Subscription subscription = new Subscription(
                SubscriptionStatus.REQUESTED,
                "a = b",
                "",
                "a=b",
                Set.of(new Endpoint("source", "host", 5671, new SubscriptionShard(exchangeName)))
        );
        Neighbour neighbour = new Neighbour("neighbour",new NeighbourCapabilities(),new NeighbourSubscriptionRequest(), new SubscriptionRequest(Set.of(subscription)));
        neighbourRepository.save(neighbour);

        Match match = new Match(
                localSubscription,
                subscription
        );
        matchRepository.save(match);
        matchDiscoveryService.createBindingsWithMatches();

        assertThat(client.queueExists(source)).isFalse();
    }

    @Test
    public void localSubscriptionWillBindToMultipleEndpointsFromNeighbour() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        String queueName = "loc-sub-queue-6";
        String exchangeName = "sub-exchange-6";
        String exchangeName2 = "sub-exchange-7";

        client.createQueue(queueName);
        client.createHeadersExchange(exchangeName);
        client.createHeadersExchange(exchangeName2);


        LocalSubscription localSubscription = new LocalSubscription(UUID.randomUUID().toString(),
                LocalSubscriptionStatus.CREATED, selector, consumerCommonName,
                Set.of(),
                Set.of(new LocalEndpoint(queueName, "my-node", 5671)));
        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider",Set.of(localSubscription));
        serviceProviderRepository.save(serviceProvider);


        Subscription subscription = new Subscription(
                SubscriptionStatus.CREATED,
                selector,
                "/neighbour-1/a",
                consumerCommonName,
                Set.of(
                        new Endpoint("source1", "host", 5671, new SubscriptionShard(exchangeName)),
                        new Endpoint("source2", "host", 5671, new SubscriptionShard(exchangeName2))
                )
        );
        Neighbour neighbour = new Neighbour(
                "neighbour-1",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(Set.of(subscription))
        );
        neighbourRepository.save(neighbour);
        Match match = new Match(localSubscription, subscription);
        matchRepository.save(match);
        matchDiscoveryService.createBindingsWithMatches();

        assertThat(client.getQueuePublishingLinks(queueName)).hasSize(2);
        assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
        assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName2));
    }


}
