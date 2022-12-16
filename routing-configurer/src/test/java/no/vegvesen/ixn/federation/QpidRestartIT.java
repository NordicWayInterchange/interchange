package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.MatchDiscoveryService;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.service.OutgoingMatchDiscoveryService;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;



@SpringBootTest(classes = {QpidClient.class, RoutingConfigurerProperties.class, QpidClientConfig.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class, ServiceProviderRouter.class})
@ContextConfiguration(initializers = {QpidRestartIT.Initializer.class})
@Testcontainers
public class QpidRestartIT extends QpidDockerBaseIT {

    private static Path testKeysPath = getFolderPath("target/test-keys" + QpidRestartIT.class.getSimpleName());

    @Container
    public static final KeysContainer keyContainer = getKeyContainer(testKeysPath,"my_ca", "localhost", "routing_configurer", "king_gustaf", "nordea");

    @Container
    public static final QpidContainer qpidContainer = getQpidTestContainer("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password","localhost")
            .dependsOn(keyContainer);

    @Autowired
    SSLContext sslContext;

    private static Logger logger = LoggerFactory.getLogger(QpidRestartIT.class);

    private static String AMQPS_URL;

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            qpidContainer.followOutput(new Slf4jLogConsumer(logger));
            String httpsUrl = qpidContainer.getHttpsUrl();
            String httpUrl = qpidContainer.getHttpUrl();
            logger.info("server url: " + httpsUrl);
            logger.info("server url: " + httpUrl);
            AMQPS_URL = qpidContainer.getAmqpsUrl();
            TestPropertyValues.of(
                    "routing-configurer.baseUrl=" + httpsUrl,
                    "routing-configurer.vhost=localhost",
                    "test.ssl.trust-store=" + testKeysPath.resolve("truststore.jks"),
                    "test.ssl.key-store=" +  testKeysPath.resolve("routing_configurer.p12")
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
    MatchDiscoveryService matchDiscoveryService;

    @MockBean
    OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

    @Autowired
    QpidClient client;

    @MockBean
    InterchangeNodeProperties properties;

    @Test
    public void testLocalSubscriptionQueuesAreAutomaticallyAddedToQpidAfterRestart() {
        String queueName = "loc-" + UUID.randomUUID().toString();

        LocalEndpoint endpoint = new LocalEndpoint(queueName, "localhost", 5671);

        String selector = "originatingCountry = 'NO'";

        LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, "");
        subscription.setLocalEndpoints(new HashSet<>(Collections.singleton(endpoint)));

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(),
                new HashSet(Collections.singleton(subscription)),
                Collections.emptySet(),
                LocalDateTime.now());

        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider));
        assertThat(client.queueExists(queueName)).isTrue();
    }

    @Test
    @Disabled
    //Capability is not set up after restart, problems with connecting to bi-queue(and everything else)
    public void testCapabilityExchangesAreAutomaticallyAddedToQpidAfterRestart() {
        String exchangeName = "cap" + UUID.randomUUID().toString();

        Capability capability = new DenmCapability("NO12345", "NO", "1.2.2", new HashSet<>(Collections.singletonList("0123")),  new HashSet<>(Collections.singletonList("5")));
        capability.setCapabilityExchangeName(exchangeName);
        capability.setRedirect(RedirectStatus.OPTIONAL);

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider));
        assertThat(client.exchangeExists(exchangeName));
    }

    @Test
    public void testDeliveryExchangesAreAutomaticallyAddedToQpidAfterRestart() {
        String exchangeName = "cap-" + UUID.randomUUID().toString();

        client.createTopicExchange(exchangeName);

        Capability capability = new DenmCapability("NO12345", "NO", "1.2.2", new HashSet<>(Collections.singletonList("0123")),  new HashSet<>(Collections.singletonList("5")));
        capability.setCapabilityExchangeName(exchangeName);
        capability.setRedirect(RedirectStatus.OPTIONAL);

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        String deliverySelector = "originatingCountry = 'NO'";

        String deliveryExchangeName = "del-" + UUID.randomUUID().toString();
        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint("localhost", 5671, deliveryExchangeName, deliverySelector);
        LocalDelivery delivery = new LocalDelivery(
                1,
                new HashSet<>(Collections.singletonList(endpoint)),
                "/delivery/1",
                deliverySelector,
                LocalDateTime.now(),
                LocalDeliveryStatus.CREATED);

        delivery.setExchangeName(deliveryExchangeName);

        serviceProvider.setDeliveries(new HashSet<>(Collections.singleton(delivery)));

        OutgoingMatch match = new OutgoingMatch(delivery, capability, "my-service-provider", OutgoingMatchStatus.SETUP_ENDPOINT);
        when(outgoingMatchDiscoveryService.findMatchesToSetupEndpointFor(any(String.class))).thenReturn(Collections.singletonList(match));
        when(outgoingMatchDiscoveryService.findMatchesFromDeliveryId(any())).thenReturn(Collections.singletonList(match));
        serviceProviderRouter.syncServiceProviders(Collections.singletonList(serviceProvider));

        assertThat(client.exchangeExists(deliveryExchangeName)).isTrue();
    }
}
