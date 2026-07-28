package no.vegvesen.ixn.federation;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.*;
import no.vegvesen.ixn.federation.routing.ServiceProviderRouter;
import no.vegvesen.ixn.federation.selector.MessageValidatingSelectorCreator;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
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
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Testcontainers
public class SPRouterQpidRestartIT extends QpidDockerBaseIT {

    private static final Logger logger = LoggerFactory.getLogger(SPRouterQpidRestartIT.class);
    public static final String HOST_NAME = getDockerHost();
    public static final CaStores stores = generateStores(getTargetFolderPathForTestClass(SPRouterQpidRestartIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf", "nordea");

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18.1")
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
        String httpUrl = qpidContainer.getHttpUrl();
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

    @Autowired
    ServiceProviderRouter serviceProviderRouter;

    @Autowired
    ServiceProviderRepository serviceProviderRepository;

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    OutgoingMatchRepository outgoingMatchRepository;

    @Autowired
    QpidClient client;

    @Autowired
    InterchangeNodeProperties interchangeNodeProperties;

    @Test
    public void testLocalSubscriptionQueueIsAutomaticallyAddedToQpidAfterRestart() {
        String queueName = "loc-" + UUID.randomUUID();
        LocalEndpoint endpoint = new LocalEndpoint(queueName, HOST_NAME, 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(UUID.randomUUID().toString(), LocalSubscriptionStatus.CREATED, selector, "", new HashSet<>(), new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());
        serviceProviderRepository.save(serviceProvider);

        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isTrue();
    }

    @Test
    public void testLocalSubscriptionQueueIsNotAutomaticallyAddedToQpidAfterRestartWhenRedirect() {
        String queueName = "neighbour-queue";
        LocalEndpoint endpoint = new LocalEndpoint(queueName, "neighbour", 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(UUID.randomUUID().toString(), LocalSubscriptionStatus.CREATED, selector, "my-service-provider", new HashSet<>(), new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        serviceProviderRepository.save(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isFalse();
        assertThat(serviceProvider.getSubscriptions()).hasSize(1);
    }

    @Test
    public void testLocalSubscriptionQueueIsNotAutomaticallyAddedToQpidAfterRestartWhenRedirectAndTearDown() {
        String queueName = "neighbour-queue";
        LocalEndpoint endpoint = new LocalEndpoint(queueName, "neighbour", 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(UUID.randomUUID().toString(), LocalSubscriptionStatus.TEAR_DOWN, selector, "my-service-provider", new HashSet<>(), new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        serviceProviderRepository.save(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isFalse();
        assertThat(serviceProvider.getSubscriptions()).hasSize(0);
    }

    @Test
    public void testLocalSubscriptionQueueIsAddedAutomaticallyToQpidWhenInRequestedAfterRestart() {
        String queueName = "loc-" + UUID.randomUUID();
        LocalEndpoint endpoint = new LocalEndpoint(queueName, HOST_NAME, 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(UUID.randomUUID().toString(), LocalSubscriptionStatus.REQUESTED, selector, "", new HashSet<>(), new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        serviceProviderRepository.save(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isTrue();
    }

    @Test
    public void testLocalSubscriptionQueuesAreNotAutomaticallyAddedToQpidAfterRestartWhenInTearDown() {
        String queueName = "loc-" + UUID.randomUUID();
        LocalEndpoint endpoint = new LocalEndpoint(queueName, HOST_NAME, 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(UUID.randomUUID().toString(), LocalSubscriptionStatus.TEAR_DOWN, selector, "", new HashSet<>(), new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());
        serviceProviderRepository.save(serviceProvider);

        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isFalse();
    }

    @Test
    public void testLocalSubscriptionQueueIsNotAddedAutomaticallyToQpidWhenInIllegalAfterRestart() {
        String queueName = "loc-" + UUID.randomUUID();
        LocalEndpoint endpoint = new LocalEndpoint(queueName, HOST_NAME, 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(UUID.randomUUID().toString(), LocalSubscriptionStatus.ILLEGAL, selector, "", new HashSet<>(), new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        serviceProviderRepository.save(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isFalse();
    }

    @Test
    public void testCapabilityExchangesAreAutomaticallyAddedToQpidAfterRestart() {
        DenmApplication denmApplication1 = new DenmApplication("NO12345", "pub-1", "NO", "1.2.2", List.of("0123"), List.of(5));
        Metadata metadata1 = new Metadata(RedirectStatus.OPTIONAL);
        Capability capability = new Capability(
                denmApplication1,
                metadata1,
                List.of(new CapabilityShard(1, "cap-" + UUID.randomUUID(), MessageValidatingSelectorCreator.makeSelector(new Capability(denmApplication1, metadata1), null)))
        );

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        serviceProviderRepository.save(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.exchangeExists(capability.getShards().getFirst().getExchangeName())).isTrue();
        assertThat(client.getQueuePublishingLinks("bi-denm")).hasSize(1);
    }

    @Test
    public void testCapabilityExchangesAreNotAutomaticallyAddedToQpidAfterRestartWhenStatusIsTearDown() {
        DenmApplication denmApplication2 = new DenmApplication("NO12345", "pub-1", "NO", "1.2.2", List.of("0123"), List.of(5));
        Metadata metadata2 = new Metadata(RedirectStatus.OPTIONAL);
        Capability capability = new Capability(
                denmApplication2,
                metadata2,
                List.of(new CapabilityShard(1, "cap-" + UUID.randomUUID(), MessageValidatingSelectorCreator.makeSelector(new Capability(denmApplication2, metadata2), null)))
        );
        capability.setStatus(CapabilityStatus.TEAR_DOWN);

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        serviceProviderRepository.save(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(capability.hasShards()).isFalse();
    }

    @Test
    @Disabled
    public void testConnectionBetweenLocalSubscriptionAndCapabilityIsAutomaticallyAddedAfterRestart() {
        DenmApplication denmApplication3 = new DenmApplication("NO12345", "pub-1", "NO", "1.2.2", List.of("0123"), List.of(5));
        Metadata metadata3 = new Metadata(RedirectStatus.OPTIONAL);
        Capability capability = new Capability(
                denmApplication3,
                metadata3,
                List.of(new CapabilityShard(1, "cap-" + UUID.randomUUID(), MessageValidatingSelectorCreator.makeSelector(new Capability(denmApplication3, metadata3), null)))
        );

        ServiceProvider serviceProvider1 = new ServiceProvider(
                "my-service-provider",
                new Capabilities(new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        serviceProviderRepository.save(serviceProvider1);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider1), client.getQpidDelta());

        assertThat(client.exchangeExists(capability.getShards().getFirst().getExchangeName())).isTrue();

        String queueName = "loc-" + UUID.randomUUID();
        LocalEndpoint endpoint = new LocalEndpoint(queueName, HOST_NAME, 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(UUID.randomUUID().toString(), LocalSubscriptionStatus.CREATED, selector, "", new HashSet<>(), new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider2 = new ServiceProvider(
                "my-service-provider-2",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        serviceProviderRepository.save(serviceProvider2);
        serviceProviderRouter.syncServiceProviders(new HashSet<>(Arrays.asList(serviceProvider1, serviceProvider2)), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isTrue();
        assertThat(client.getQueuePublishingLinks(queueName)).hasSize(1);
    }

    @Test
    public void testDeliveryExchangesAreAutomaticallyAddedToQpidAfterRestart() {
        Capability capability = new Capability(
                new DenmApplication(
                        "NO12345",
                        "pub-1",
                        "NO",
                        "1.2.2",
                        List.of("0123"),
                        List.of(5)
                ),
                new Metadata(RedirectStatus.OPTIONAL)
        );


        String deliverySelector = "originatingCountry = 'NO'";
        String deliveryExchangeName = "del-" + UUID.randomUUID();
        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(HOST_NAME, 5671, deliveryExchangeName);
        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                new HashSet<>(Collections.singletonList(endpoint)),
                deliverySelector,
                LocalDeliveryStatus.CREATED
        );

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(Set.of(capability)),
                Set.of(),
                Set.of(delivery),
                LocalDateTime.now());
        serviceProviderRepository.save(serviceProvider);

        OutgoingMatch match = new OutgoingMatch(delivery, capability, "my-service-provider");
        outgoingMatchRepository.save(match);

        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.exchangeExists(deliveryExchangeName)).isTrue();
    }

    @Test
    public void testDeliveryExchangeIsNotAutomaticallyAddedToQpidAfterRestart() {
        Capability capability = new Capability(
                new DenmApplication(
                        "NO12345",
                        "pub-1",
                        "NO",
                        "1.2.2",
                        List.of("0123"),
                        List.of(5)
                ),
                new Metadata(RedirectStatus.OPTIONAL)
        );


        String deliverySelector = "originatingCountry = 'SE'";
        String deliveryExchangeName = "del-" + UUID.randomUUID();
        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(HOST_NAME, 5671, deliveryExchangeName);
        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(endpoint),
                deliverySelector,
                LocalDeliveryStatus.NO_OVERLAP
        );

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(Set.of(capability)),
                Set.of(),
                Set.of(delivery),
                LocalDateTime.now());
        serviceProviderRepository.save(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.exchangeExists(deliveryExchangeName)).isFalse();
    }
}
