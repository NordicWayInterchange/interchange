package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
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
import static org.mockito.Mockito.when;


@SpringBootTest(classes = {QpidClient.class, RoutingConfigurerProperties.class, QpidClientConfig.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class, RoutingConfigurer.class})
@ContextConfiguration(initializers = {RoutingConfigurerQpidRestartIT.Initializer.class})
@Testcontainers
public class RoutingConfigurerQpidRestartIT extends QpidDockerBaseIT {

    private static Path testKeysPath = getFolderPath("target/test-keys" + RoutingConfigurerQpidRestartIT.class.getSimpleName());

    @Container
    public static final KeysContainer keyContainer = getKeyContainer(testKeysPath,"my_ca", "localhost", "routing_configurer", "king_gustaf", "nordea");

    @Container
    public static final QpidContainer qpidContainer = getQpidTestContainer("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password","localhost")
            .dependsOn(keyContainer);

    @Autowired
    SSLContext sslContext;

    private static Logger logger = LoggerFactory.getLogger(RoutingConfigurerQpidRestartIT.class);

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
        String exchangeName = "cap-" + UUID.randomUUID().toString();

        CapabilitySplit capability = new CapabilitySplit(
                new DenmApplication(
                        "NO12345",
                        "pub-1",
                        "NO",
                        "1.2.2",
                        new HashSet<>(Arrays.asList("0123")),
                        new HashSet<>(Arrays.asList(5))
                ),
                new Metadata(RedirectStatus.OPTIONAL)
        );

        capability.setCapabilityExchangeName(exchangeName);

        client.createTopicExchange(exchangeName);

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        String queueName = "sub-" + UUID.randomUUID();

        NeighbourSubscription sub = new NeighbourSubscription("originatingCountry = 'NO'", NeighbourSubscriptionStatus.CREATED, "neighbour");
        sub.setQueueName(queueName);

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.ESTABLISHED, Collections.singleton(sub)),
                new SubscriptionRequest(Collections.emptySet()));


        when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singletonList(serviceProvider));
        routingConfigurer.setupNeighbourRouting(neighbour);

        assertThat(client.queueExists(queueName)).isFalse();
    }

    @Test
    public void testSetupRedirectNeighbourSubscriptionRoutingAfterRestart() {
        String exchangeName = "cap-" + UUID.randomUUID().toString();

        CapabilitySplit capability = new CapabilitySplit(
                new DenmApplication(
                        "NO2345",
                        "pub-1",
                        "NO",
                        "1.2.2",
                        new HashSet<>(Arrays.asList("0123")),
                        new HashSet<>(Arrays.asList(5))
                ),
                new Metadata(RedirectStatus.OPTIONAL)
        );

        capability.setCapabilityExchangeName(exchangeName);

        client.createTopicExchange(exchangeName);

        ServiceProvider serviceProvider = new ServiceProvider(
                "my-service-provider",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Collections.singletonList(capability))),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now());

        String queueName = "re-" + UUID.randomUUID();

        NeighbourSubscription sub = new NeighbourSubscription("originatingCountry = 'NO'", NeighbourSubscriptionStatus.CREATED, "neighbour-consumer");
        sub.setQueueName(queueName);

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
                new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.ESTABLISHED, Collections.singleton(sub)),
                new SubscriptionRequest(Collections.emptySet()));


        when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singletonList(serviceProvider));
        routingConfigurer.setupNeighbourRouting(neighbour);

        assertThat(client.queueExists(queueName)).isFalse();
    }
}
