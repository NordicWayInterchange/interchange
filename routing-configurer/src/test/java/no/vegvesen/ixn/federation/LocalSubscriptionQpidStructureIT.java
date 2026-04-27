package no.vegvesen.ixn.federation;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidDelta;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.routing.ServiceProviderRouter;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest
@Transactional
@Testcontainers
public class LocalSubscriptionQpidStructureIT extends QpidDockerBaseIT {

    public static final String CONFIGURER_USER = "routing_configurer";

    private static final Logger logger = LoggerFactory.getLogger(LocalSubscriptionQpidStructureIT.class);

    public static final String SP_NAME = "sp-1";

    public static final String HOST_NAME = getDockerHost();

    private static final CaStores stores = generateStores(getTargetFolderPathForTestClass(LocalSubscriptionQpidStructureIT.class),"my_ca", HOST_NAME, CONFIGURER_USER, SP_NAME);

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
        registry.add("interchange.node-provider.broker-external-name", () -> HOST_NAME);
        registry.add("interchange.node-provider.message-channel-port", qpidContainer::getAmqpsPort);
        registry.add("spring.ssl.bundle.jks.qpid-client.keystore.location", () -> routingConfigurerStore.path().toString());
        registry.add("spring.ssl.bundle.jks.qpid-client.keystore.password", routingConfigurerStore::password);
        registry.add("spring.ssl.bundle.jks.qpid-client.truststore.location", () -> caStore.truststoreName().toString());
        registry.add("spring.ssl.bundle.jks.qpid-client.truststore.password", caStore::truststorePassword);
    }

    @Autowired
    ServiceProviderRepository serviceProviderRepository;

    @Autowired
    PrivateChannelRepository privateChannelRepository;

    @Autowired
    QpidClient client;

    @Autowired
    ServiceProviderRouter router;

    //NOTE: This test is better elsewhere
    @Test
    public void setupServiceProviderQueueAndConnect() {
        System.out.println(qpidContainer.getHttpUrl());
        ServiceProvider serviceProvider = new ServiceProvider(
                SP_NAME,
                new Capabilities(),
                Set.of(new LocalSubscription(
                        LocalSubscriptionStatus.REQUESTED,
                        "originatingCountry = 'NO'",
                        HOST_NAME,
                        Collections.emptySet())
                ),
                LocalDateTime.now());
        serviceProviderRepository.save(serviceProvider);
        QpidDelta delta = client.getQpidDelta();
        router.syncServiceProviders(List.of(serviceProvider), delta);
        LocalEndpoint actualEndpoint = serviceProvider.getSubscriptions().stream().findFirst().orElseThrow().getLocalEndpoints().stream().findFirst().orElseThrow();
        assertThat(actualEndpoint).isNotNull();
        SSLContext sslContext = sslClientContext(stores,SP_NAME);
        assertThatNoException().isThrownBy(() -> {
            try (Sink sink = new Sink(
                    String.format("amqps://%s:%d",actualEndpoint.getHost(),actualEndpoint.getPort()),
                    actualEndpoint.getSource(),
                    sslContext,
                    System.out::println
            ))  {
                sink.start();

            }
        });
    }
}
