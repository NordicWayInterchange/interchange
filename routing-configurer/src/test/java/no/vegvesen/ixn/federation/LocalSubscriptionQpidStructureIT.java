package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.QpidDelta;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.routing.ServiceProviderRouter;
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
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
        QpidClient.class,
        QpidClientConfig.class,
        TestSSLContextConfigGeneratedExternalKeys.class,
        TestSSLProperties.class,
        RoutingConfigurerProperties.class,
        InterchangeNodeProperties.class,
        ServiceProviderRouter.class
        })
@ContextConfiguration(initializers = {LocalSubscriptionQpidStructureIT.Initializer.class})
@Testcontainers
public class LocalSubscriptionQpidStructureIT extends QpidDockerBaseIT {

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            qpidContainer.followOutput(new Slf4jLogConsumer(logger));
            String httpsUrl = qpidContainer.getHttpsUrl();
            String httpUrl = qpidContainer.getHttpUrl();
            logger.info("server url: " + httpUrl);
            TestPropertyValues.of(
                    "routing-configurer.baseUrl=" + httpsUrl,
                    "routing-configurer.vhost=localhost",
                    "test.ssl.trust-store=" + keysStructure.getKeysOutputPath().resolve("truststore.jks"),
                    "test.ssl.key-store=" +  keysStructure.getKeysOutputPath().resolve("routing_configurer.p12"),
                    "interchange.node-provider.name=" + qpidContainer.getHost(),
                    "interchange.node-provider.messageChannelPort=" + qpidContainer.getAmqpsPort()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }


    private static Logger logger = LoggerFactory.getLogger(LocalSubscriptionQpidStructureIT.class);


    public static final String SP_NAME = "sp-1";

    private static KeysStructure keysStructure = generateKeys(LocalSubscriptionQpidStructureIT.class,"my_ca", "localhost", "routing_configurer", SP_NAME);

    @Container
    public static final QpidContainer qpidContainer = getQpidTestContainer("qpid",
            keysStructure,
            "localhost");


    //TODO would be nic to be able to do without it :-)
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

    @Autowired
    ServiceProviderRouter router;

    @Test
    public void setupServiceProviderQueueAndConnect() {
        System.out.println(qpidContainer.getHttpUrl());
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                SP_NAME,
                new Capabilities(),
                Collections.singleton(new LocalSubscription(
                        1,
                        LocalSubscriptionStatus.REQUESTED,
                        "originatingCountry = 'NO'",
                        "my-node",
                        Collections.emptySet(),
                        Collections.emptySet())
                ),
                LocalDateTime.now());
        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        QpidDelta delta = client.getQpidDelta();
        router.syncServiceProviders(Collections.singleton(serviceProvider), delta);
        LocalEndpoint actualEndpoint = null;
        for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
            for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
                assertThat(endpoint.getSource()).isNotNull();
                assertThat(endpoint.getHost()).isNotNull();
                assertThat(endpoint.getPort()).isNotNull();
                System.out.println(endpoint);
                actualEndpoint = endpoint;
            }
        }
        assertThat(actualEndpoint).isNotNull();
        SSLContext sslContext = sslClientContext(keysStructure,SP_NAME);
        try (Sink sink = new Sink(
                String.format("amqps://%s:%d",actualEndpoint.getHost(),actualEndpoint.getPort()),
                actualEndpoint.getSource(),
                sslContext,
                message -> System.out.println(message)
        ))  {
            sink.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
