package no.vegvesen.ixn.federation.service.routing.localsubscription;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@SpringBootTest
@Transactional
public class LocalSubscriptionServiceIT {
    public static final String HOST_NAME = QpidDockerBaseIT.getDockerHost();
    private static final ClusterKeyGenerator.CaStores stores = QpidDockerBaseIT.generateStores(QpidDockerBaseIT.getTargetFolderPathForTestClass(LocalSubscriptionService.class),"my_ca", HOST_NAME, "routing_configurer");

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
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", ()-> "create-drop");
        registry.add("routing-configurer.interval",()->"999");
        registry.add("routing-configurer.baseUrl", qpidContainer::getHttpsUrl);
        registry.add("routing-configurer.vhost",() -> HOST_NAME);
        ClusterKeyGenerator.ClientStore routingConfigurerStore = ClusterKeyGenerator.getClientStore("routing_configurer", stores.clientStores().stream());
        ClusterKeyGenerator.CaStore caStore = stores.trustStore();
        registry.add("spring.ssl.bundle.jks.qpid-client.keystore.location", () -> routingConfigurerStore.path().toString());
        registry.add("spring.ssl.bundle.jks.qpid-client.keystore.password", routingConfigurerStore::password);
        registry.add("spring.ssl.bundle.jks.qpid-client.truststore.location", () -> caStore.truststoreName().toString());
        registry.add("spring.ssl.bundle.jks.qpid-client.truststore.password", caStore::truststorePassword);
    }

    @Autowired
    private RoutingConfigurerProperties routingConfigurerProperties;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private MatchRepository matchRepository;

    private LocalSubscriptionService localSubscriptionService;
    private QpidClient client;

    @BeforeEach
    public void setup() {
        client = new QpidClient(
                new QpidClientConfig(
                    QpidDockerBaseIT.sslClientContext(stores,"routing_configurer")
                ).qpidRestTemplate(),
               routingConfigurerProperties
        );
        localSubscriptionService = new LocalSubscriptionService(serviceProviderRepository,matchRepository,client);
    }

    @Test
    public void newServiceProviderCanAddSubscriptionsThatWillBindToTheQueue() {
        LocalSubscription localSubscription1 = new LocalSubscription(
                LocalSubscriptionStatus.REQUESTED,
                "messageType = 'DATEX2' and originatingCountry = 'NO'",
                HOST_NAME
        );
        ServiceProvider nordea = new ServiceProvider("nordea", Set.of(localSubscription1));

        serviceProviderRepository.save(nordea);

        nordea = localSubscriptionService.syncSubscriptions(HOST_NAME,qpidContainer.getAmqpsPort().toString(),nordea, client.getQpidDelta());
        Set<LocalEndpoint> endpoints = nordea.getSubscriptions().stream().flatMap(s -> s.getLocalEndpoints().stream()).collect(Collectors.toSet());
        assertThat(endpoints).hasSize(1);

        LocalSubscription localSubscription2 = new LocalSubscription(
                LocalSubscriptionStatus.REQUESTED,
                "messageType = 'DATEX2' and originatingCountry = 'FI'",
                HOST_NAME
        );

        nordea.addLocalSubscription(localSubscription2);
        nordea = localSubscriptionService.syncSubscriptions(HOST_NAME, qpidContainer.getAmqpsPort().toString(),nordea, client.getQpidDelta());
        Set<LocalEndpoint> endpoints2 = nordea.getSubscriptions().stream()
                .filter(s -> s.getSelector().contains("'FI'"))
                .flatMap(s -> s.getLocalEndpoints().stream())
                .collect(Collectors.toSet());
        assertThat(endpoints2).hasSize(1);
    }

    @Test
    public void removeSubscriptionWhenSelectorIsInvalid(){
        ServiceProvider king_gustaf = new ServiceProvider(
                "king_gustaf",
                Set.of(
                        new LocalSubscription(
                                LocalSubscriptionStatus.ERROR,
                                "1=1",
                                HOST_NAME
                        ),
                        new LocalSubscription(
                                LocalSubscriptionStatus.ERROR,
                                "messageType = 'DATEX2'",
                                HOST_NAME
                        ),
                        new LocalSubscription(
                                LocalSubscriptionStatus.REQUESTED,
                                "messageType = 'DATEX23'",
                                HOST_NAME
                        )
                )

        );
        serviceProviderRepository.save(king_gustaf);
        localSubscriptionService.syncSubscriptions(HOST_NAME,qpidContainer.getAmqpsPort().toString(),king_gustaf, client.getQpidDelta());
        localSubscriptionService.removeUnwantedSubscriptions(king_gustaf);
        assertThat(king_gustaf.getSubscriptions().size()).isEqualTo(1);
    }

    @Test
    public void tearDownQueueWhenLocalSubscriptionIsDeletedAfterMatch() {
        String serviceProviderName = "my-service-provider";
        String selector = "a=b";
        String queueName = "my-queue";
        InterchangeNodeProperties nodeProperties = new InterchangeNodeProperties(HOST_NAME,qpidContainer.getAmqpsPort().toString());
        LocalSubscription localSubscription = new LocalSubscription(UUID.randomUUID().toString(), LocalSubscriptionStatus.TEAR_DOWN, selector, "my-node", new HashSet<>(),
                Collections.singleton(
                        new LocalEndpoint(queueName,
                                nodeProperties.getName(),
                                Integer.parseInt(nodeProperties.getMessageChannelPort())
                        )));
        client.createQueue(queueName);
        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName,Set.of(localSubscription));
        serviceProviderRepository.save(serviceProvider);
        localSubscriptionService.syncSubscriptions(HOST_NAME,qpidContainer.getAmqpsPort().toString(),serviceProvider, client.getQpidDelta());

        assertThat(client.queueExists(queueName)).isFalse();
    }

    @Test
    public void localSubscriptionConnectsToCapabilityExchange() {

        LocalEndpoint endpoint = new LocalEndpoint("endpoint-1", HOST_NAME, qpidContainer.getAmqpsPort());
        LocalSubscription subscription = new LocalSubscription(

                LocalSubscriptionStatus.CREATED,
                "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')",
                HOST_NAME,
                Set.of(),
                Set.of(endpoint)
        );
        client.createQueue("endpoint-1");

        CapabilityShard shard = new CapabilityShard(1, "cap-ex8", "publicationId = 'pub-1'");
        Capability denmCapability = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "1.0",
                        List.of("1234"),
                        List.of(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL),
                List.of(shard)
        );
        client.createHeadersExchange("cap-ex8");
        denmCapability.setStatus(CapabilityStatus.CREATED);

        ServiceProvider mySP = new ServiceProvider("my-sp",Set.of(subscription));
        serviceProviderRepository.save(mySP);
        Capabilities capabilities = new Capabilities(Set.of(denmCapability));
        ServiceProvider otherSP = new ServiceProvider("other-sp",capabilities);
        serviceProviderRepository.save(otherSP);


        localSubscriptionService.syncLocalSubscriptionsToServiceProviderCapabilities(mySP, client.getQpidDelta(), Collections.singleton(otherSP));

        assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().orElseThrow().getSource())).hasSize(1);
        assertThat(subscription.getConnections()).hasSize(1);
    }


    @Test
    public void localSubscriptionConnectsToCapabilityWithMultipleShards() {

        LocalEndpoint endpoint = new LocalEndpoint("endpoint-2", HOST_NAME, qpidContainer.getAmqpsPort());
        LocalSubscription subscription = new LocalSubscription(
                LocalSubscriptionStatus.CREATED,
                "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')",
                "my-node",
                Set.of(),
                Set.of(endpoint)
        );
        client.createQueue("endpoint-2");

        Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);

        CapabilityShard shard1 = new CapabilityShard(1, "cap-ex9", "publicationId = 'pub-1'");
        client.createHeadersExchange("cap-ex9");

        CapabilityShard shard2 = new CapabilityShard(2, "cap-ex10", "publicationId = 'pub-1'");
        client.createHeadersExchange("cap-ex10");

        CapabilityShard shard3 = new CapabilityShard(3, "cap-ex11", "publicationId = 'pub-1'");
        client.createHeadersExchange("cap-ex11");

        Capability denmCapability = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "1.0",
                        List.of("1234"),
                        List.of(6)
                ),
                metadata,
                List.of(shard1, shard2, shard3)
        );
        denmCapability.setStatus(CapabilityStatus.CREATED);

        ServiceProvider mySP = new ServiceProvider("my-sp",Set.of(subscription));
        serviceProviderRepository.save(mySP);
        Capabilities capabilities = new Capabilities(Collections.singleton(denmCapability));
        ServiceProvider otherSP = new ServiceProvider("other-sp",capabilities);
        serviceProviderRepository.save(otherSP);

        localSubscriptionService.syncLocalSubscriptionsToServiceProviderCapabilities(mySP, client.getQpidDelta(), Collections.singleton(otherSP));


        assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().orElseThrow().getSource())).hasSize(3);
        assertThat(subscription.getLocalEndpoints()).hasSize(1);
        assertThat(subscription.getConnections()).hasSize(3);
    }

    @Test
    public void testSetupBindingWhenLocalConnectionExistsButCapExchangeIsNotBoundToLocalSubscriptionQueue() {
        String publisherId = "NO98765";
        String publicationName = "sdkjsd";
        String publicationId = String.join(":", publisherId, publicationName);
        String capabilityExchangeName = "binding-test-exchange-1";
        String subscriptionQueue = "my-local-subscription-queue";
        //create local subscription queue
        client.createQueue(subscriptionQueue);
        //create capability exchange (but not bound to local subscription)
        client.createHeadersExchange(capabilityExchangeName);
        Capability capability = new Capability(
                UUID.randomUUID().toString(),
                new DenmApplication(
                        publisherId,
                        publicationId,
                        "NO",
                        "1.0",
                        List.of(),
                        List.of(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL),
                List.of(
                        new CapabilityShard(
                                1,
                                capabilityExchangeName,
                                "publicationId = '" + publicationId + "' and shardId = 1"
                        )
                )
        );
        capability.setStatus(CapabilityStatus.CREATED);
        ServiceProvider serviceProvider = new ServiceProvider(
                "serviceProviderBindingTest",
                new Capabilities(
                        Set.of(
                                capability
                        )

                ),
                Set.of(
                        new LocalSubscription(
                                UUID.randomUUID().toString(),
                                LocalSubscriptionStatus.CREATED,
                                "publicationId = '" + publicationId + "'",
                                "myserver",
                                Set.of(
                                        new LocalConnection(
                                                capabilityExchangeName,
                                                subscriptionQueue
                                        )
                                ),
                                Set.of(
                                        new LocalEndpoint(
                                                subscriptionQueue,
                                                "myhost",
                                                5671
                                        )
                                )
                        )
                ),
                LocalDateTime.now()
        );
        localSubscriptionService.syncLocalSubscriptionsToServiceProviderCapabilities(serviceProvider,client.getQpidDelta(),List.of(serviceProvider));
        Exchange exchange = client.getExchange(capabilityExchangeName);
        List<String> destinations = exchange.getBindings().stream().map(Binding::getDestination).toList();
        assertThat(destinations).contains(subscriptionQueue);
    }

    @Test
    public void testCreateConnectionIfItDoesNotExistButBindingExists() {
        String publisherId = "NO87654";
        String publicationName = "abjasd";
        String publicationId = String.join(":", publisherId, publicationName);
        String capabilityExchangeName = "binding-test-exchange-2";
        String subscriptionQueue = "my-local-subscription-queue-2";
        //create local subscription queue
        client.createQueue(subscriptionQueue);
        //create capability exchange (but not bound to local subscription)
        client.createHeadersExchange(capabilityExchangeName);
        String selector = "publicationId = '" + publicationId + "'";
        client.addBinding(capabilityExchangeName,new Binding(capabilityExchangeName,subscriptionQueue,new Filter(selector)));
        Capability capability = new Capability(
                UUID.randomUUID().toString(),
                new DenmApplication(
                        publisherId,
                        publicationId,
                        "NO",
                        "1.0",
                        List.of(),
                        List.of(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL),
                List.of(
                        new CapabilityShard(
                                1,
                                capabilityExchangeName,
                                selector
                        )
                )
        );
        capability.setStatus(CapabilityStatus.CREATED);
        ServiceProvider serviceProvider = new ServiceProvider(
                "serviceProviderBindingTest",
                new Capabilities(
                        Set.of(
                                capability
                        )

                ),
                Set.of(
                        new LocalSubscription(
                                UUID.randomUUID().toString(),
                                LocalSubscriptionStatus.CREATED,
                                selector,
                                "myserver",
                                Set.of(),
                                Set.of(
                                        new LocalEndpoint(
                                                subscriptionQueue,
                                                "myhost",
                                                5671
                                        )
                                )
                        )
                ),
                LocalDateTime.now()
        );
        localSubscriptionService.syncLocalSubscriptionsToServiceProviderCapabilities(serviceProvider,client.getQpidDelta(),List.of(serviceProvider));
        Exchange exchange = client.getExchange(capabilityExchangeName);
        List<String> destinations = exchange.getBindings().stream().map(Binding::getDestination).toList();
        System.out.println(destinations);
        assertThat(destinations).contains(subscriptionQueue);
        List<LocalConnection> connections = serviceProvider.getSubscriptions().stream().flatMap(subscription -> subscription.getConnections().stream()).toList();
        assertThat(connections).contains(new LocalConnection(capabilityExchangeName,subscriptionQueue));

    }

}
