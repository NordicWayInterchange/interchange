package no.vegvesen.ixn.federation.routing;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.TestSSLContextConfigGeneratedExternalKeys;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.model.capability.Shard;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import no.vegvesen.ixn.federation.TestSSLContextConfigGeneratedExternalKeys;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = {QpidClient.class, RoutingConfigurerProperties.class, QpidClientConfig.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class, RoutingConfigurer.class})
public class RoutingConfigurerQpidRestartIT extends QpidDockerBaseIT {


    public static final String HOST_NAME = getDockerHost();
    private static final CaStores stores = generateStores(
            getTargetFolderPathForTestClass(RoutingConfigurerIT.class),
            "my_ca",
            HOST_NAME,
            "routing_configurer",
            "king_gustaf"
    );


    @Qualifier("getTestSslContext")
    @Autowired
    SSLContext sslContext;

    private static final Logger logger = LoggerFactory.getLogger(RoutingConfigurerQpidRestartIT.class);

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
        logger.info("server url: {}", httpUrl);
        registry.add("routing-configurer.baseUrl", () -> httpsUrl);
        registry.add("routing-configurer.vhost", () -> "localhost");
        registry.add("test.ssl.trust-store", () -> getTrustStorePath(stores));
        registry.add("test.ssl.key-store", () -> getClientStorePath("routing_configurer", stores.clientStores()));
    }

    @BeforeAll
    static void setUp(){
        qpidContainer.start();
    }

    @MockBean
    NeighbourService neighbourService;

    @Autowired
    RoutingConfigurer routingConfigurer;

    @MockBean
    ListenerEndpointRepository listenerEndpointRepository;

    @Autowired
    QpidClient client;

    @MockBean
    ServiceProviderRouter serviceProviderRouter;

    @MockBean
    InterchangeNodeProperties properties;

    @Test
    public void testSetupRegularNeighbourSubscriptionRoutingAfterRestart() {
        String exchangeName = "cap-" + UUID.randomUUID();

        Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
        Shard shard = new Shard(1, exchangeName, "publicationId = 'pub-1'");
        metadata.setShards(Collections.singletonList(shard));

        Capability capability = new Capability(
                new DenmApplication(
                        "NO12345",
                        "pub-1",
                        "NO",
                        "1.2.2",
                        List.of("0123"),
                        List.of(5)
                ),
                metadata
        );

        client.createHeadersExchange(exchangeName);

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        NeighbourSubscription sub = new NeighbourSubscription("originatingCountry = 'NO'", NeighbourSubscriptionStatus.CREATED, "neighbour");

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new NeighbourSubscriptionRequest(Collections.singleton(sub)),
                new SubscriptionRequest(Collections.emptySet()));


        when(neighbourService.getMessagePort()).thenReturn("5671");
        when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singletonList(serviceProvider));
        routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

        assertThat(sub.getEndpoints()).isEmpty();
    }

    @Test
    public void testSetupRedirectNeighbourSubscriptionRoutingAfterRestart() {
        String exchangeName = "cap-" + UUID.randomUUID();

        Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
        Shard shard = new Shard(1, exchangeName, "publicationId = 'pub-1'");
        metadata.setShards(Collections.singletonList(shard));

        Capability capability = new Capability(
                new DenmApplication(
                        "NO2345",
                        "pub-1",
                        "NO",
                        "1.2.2",
                        List.of("0123"),
                        List.of(5)
                ),
                metadata
        );

        client.createHeadersExchange(exchangeName);

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        NeighbourSubscription sub = new NeighbourSubscription("originatingCountry = 'NO'", NeighbourSubscriptionStatus.CREATED, "neighbour-consumer");

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new NeighbourSubscriptionRequest(Collections.singleton(sub)),
                new SubscriptionRequest(Collections.emptySet()));


        when(neighbourService.getMessagePort()).thenReturn("5671");
        when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singletonList(serviceProvider));
        routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

        assertThat(sub.getEndpoints()).isEmpty();
    }
}
