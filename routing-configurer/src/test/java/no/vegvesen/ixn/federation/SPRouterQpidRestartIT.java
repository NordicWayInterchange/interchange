package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.*;
import no.vegvesen.ixn.federation.routing.ServiceProviderRouter;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;



@SpringBootTest(classes = {QpidClient.class, RoutingConfigurerProperties.class, QpidClientConfig.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class, ServiceProviderRouter.class})
@ContextConfiguration(initializers = {SPRouterQpidRestartIT.Initializer.class})
@Testcontainers
public class SPRouterQpidRestartIT extends QpidDockerBaseIT {

    public static final String HOST_NAME = getDockerHost();
    public static final CaStores stores = generateStores(getTargetFolderPathForTestClass(SPRouterQpidRestartIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf", "nordea");

    @Container
    public static final QpidContainer qpidContainer = getQpidTestContainer(
            stores,
            HOST_NAME,
            HOST_NAME,
            Path.of("qpid")
            );

    @Qualifier("getTestSslContext")
    @Autowired
    SSLContext sslContext;

    private static final Logger logger = LoggerFactory.getLogger(SPRouterQpidRestartIT.class);

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            qpidContainer.followOutput(new Slf4jLogConsumer(logger));
            String httpsUrl = qpidContainer.getHttpsUrl();
            String httpUrl = qpidContainer.getHttpUrl();
            logger.info("server url: {}", httpsUrl);
            logger.info("server url: {}", httpUrl);
            TestPropertyValues.of(
                    "routing-configurer.baseUrl=" + httpsUrl,
                    "routing-configurer.vhost=localhost",
                    "test.ssl.trust-store=" + getTrustStorePath(stores),
                    "test.ssl.key-store=" +  getClientStorePath("routing_configurer",stores.clientStores())
            ).applyTo(configurableApplicationContext.getEnvironment());
        }

    }

    @MockBean
    NeighbourService neighbourService;

    @Autowired
    ServiceProviderRouter serviceProviderRouter;

    @MockBean
    ServiceProviderRepository serviceProviderRepository;

    @MockBean
    MatchRepository matchRepository;

    @MockBean
    OutgoingMatchRepository outgoingMatchRepository;

    @MockBean
    PrivateChannelRepository privateChannelRepository;

    @Autowired
    QpidClient client;

    @MockBean
    InterchangeNodeProperties properties;

    @Test
    public void testLocalSubscriptionQueueIsAutomaticallyAddedToQpidAfterRestart() {
        String queueName = "loc-" + UUID.randomUUID();
        LocalEndpoint endpoint = new LocalEndpoint(queueName, HOST_NAME, 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, "");
        subscription.setLocalEndpoints(new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isTrue();
    }

    @Test
    public void testLocalSubscriptionQueueIsNotAutomaticallyAddedToQpidAfterRestartWhenRedirect() {
        String queueName = "neighbour-queue";
        LocalEndpoint endpoint = new LocalEndpoint(queueName, "neighbour", 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, "my-service-provider");
        subscription.setLocalEndpoints(new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isFalse();
        assertThat(serviceProvider.getSubscriptions()).hasSize(1);
    }

    @Test
    public void testLocalSubscriptionQueueIsNotAutomaticallyAddedToQpidAfterRestartWhenRedirectAndTearDown() {
        String queueName = "neighbour-queue";
        LocalEndpoint endpoint = new LocalEndpoint(queueName, "neighbour", 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.TEAR_DOWN, selector, "my-service-provider");
        subscription.setLocalEndpoints(new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        when(matchRepository.findAllByLocalSubscriptionId(anyInt())).thenReturn(Collections.emptyList());
        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isFalse();
        assertThat(serviceProvider.getSubscriptions()).hasSize(0);
    }

    @Test
    public void testLocalSubscriptionQueueIsAddedAutomaticallyToQpidWhenInRequestedAfterRestart() {
        String queueName = "loc-" + UUID.randomUUID();
        LocalEndpoint endpoint = new LocalEndpoint(queueName, HOST_NAME, 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, selector, "");
        subscription.setLocalEndpoints(new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isTrue();
    }

    @Test
    public void testLocalSubscriptionQueuesAreNotAutomaticallyAddedToQpidAfterRestartWhenInTearDown() {
        String queueName = "loc-" + UUID.randomUUID();
        LocalEndpoint endpoint = new LocalEndpoint(queueName, HOST_NAME, 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.TEAR_DOWN, selector, "");
        subscription.setLocalEndpoints(new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        when(matchRepository.findAllByLocalSubscriptionId(anyInt())).thenReturn(Collections.emptyList());
        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isFalse();
    }

    @Test
    public void testLocalSubscriptionQueueIsNotAddedAutomaticallyToQpidWhenInIllegalAfterRestart() {
        String queueName = "loc-" + UUID.randomUUID();
        LocalEndpoint endpoint = new LocalEndpoint(queueName, HOST_NAME, 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.ILLEGAL, selector, "");
        subscription.setLocalEndpoints(new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        when(matchRepository.findAllByLocalSubscriptionId(anyInt())).thenReturn(Collections.emptyList());
        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.queueExists(queueName)).isFalse();
    }

    @Test
    public void testCapabilityExchangesAreAutomaticallyAddedToQpidAfterRestart() {
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

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.exchangeExists(capability.getMetadata().getShards().get(0).getExchangeName())).isTrue();
        assertThat(client.getQueuePublishingLinks("bi-queue")).hasSize(1);
    }

    @Test
    public void testCapabilityExchangesAreNotAutomaticallyAddedToQpidAfterRestartWhenStatusIsTearDown() {
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
        capability.setStatus(CapabilityStatus.TEAR_DOWN);

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(capability.hasShards()).isFalse();
    }

    @Test
    public void testConnectionBetweenLocalSubscriptionAndCapabilityIsAutomaticallyAddedAfterRestart() {
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

        ServiceProvider serviceProvider1 = new ServiceProvider(
                "my-service-provider",
                new Capabilities(new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider1);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider1), client.getQpidDelta());

        assertThat(client.exchangeExists(capability.getMetadata().getShards().get(0).getExchangeName())).isTrue();

        String queueName = "loc-" + UUID.randomUUID();
        LocalEndpoint endpoint = new LocalEndpoint(queueName, HOST_NAME, 5671);
        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, "");
        subscription.setLocalEndpoints(new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider2 = new ServiceProvider(
                "my-service-provider-2",
                new Capabilities(),
                Collections.singleton(subscription),
                Collections.emptySet(),
                LocalDateTime.now());

        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider2);
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

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        String deliverySelector = "originatingCountry = 'NO'";
        String deliveryExchangeName = "del-" + UUID.randomUUID();
        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(HOST_NAME, 5671, deliveryExchangeName);
        LocalDelivery delivery = new LocalDelivery(
                1,
                new HashSet<>(Collections.singletonList(endpoint)),
                "/delivery/1",
                deliverySelector,
                LocalDeliveryStatus.CREATED);

        delivery.setExchangeName(deliveryExchangeName);
        serviceProvider.setDeliveries(new HashSet<>(Collections.singleton(delivery)));

        OutgoingMatch match = new OutgoingMatch(delivery, capability, "my-service-provider");
        when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Collections.singletonList(match));
        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
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

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        String deliverySelector = "originatingCountry = 'SE'";
        String deliveryExchangeName = "del-" + UUID.randomUUID();
        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(HOST_NAME, 5671, deliveryExchangeName);
        LocalDelivery delivery = new LocalDelivery(
                1,
                new HashSet<>(Collections.singletonList(endpoint)),
                "/delivery/1",
                deliverySelector,
                LocalDeliveryStatus.NO_OVERLAP);

        delivery.setExchangeName(deliveryExchangeName);

        serviceProvider.setDeliveries(new HashSet<>(Collections.singleton(delivery)));

        when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Collections.emptyList());
        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider), client.getQpidDelta());
        assertThat(client.exchangeExists(deliveryExchangeName)).isFalse();
    }
}
