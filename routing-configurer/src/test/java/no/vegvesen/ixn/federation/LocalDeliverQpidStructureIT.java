package no.vegvesen.ixn.federation;

import jakarta.jms.JMSException;
import jakarta.transaction.Transactional;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.*;
import no.vegvesen.ixn.federation.routing.ServiceProviderRouter;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Testcontainers
public class LocalDeliverQpidStructureIT extends QpidDockerBaseIT {


    public static final String HOST_NAME = getDockerHost();
    private static final ClusterKeyGenerator.CaStores stores = generateStores(getTargetFolderPathForTestClass(ServiceProviderRouterIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf");

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18.1")
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
        System.out.println("Qpid admin UI: " + qpidContainer.getHttpUrl());
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
    QpidClient client;


    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private OutgoingMatchRepository outgoingMatchRepository;

    @Autowired
    ServiceProviderRouter router;



    @Test
    public void newServiceProviderCanReadDedicatedOutQueue() {
        String source = "king_gustaf_source";

        Capability capability = new Capability(
                new DatexApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "1.0",
                        List.of("0122"),
                        "publicationType",
                        "publisherName"
                ),
                new Metadata(RedirectStatus.OPTIONAL)
        );
        capability.setStatus(CapabilityStatus.CREATED);
        String exchangeName = "myexchange";
        LocalDelivery localDelivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(new LocalDeliveryEndpoint(
                        qpidContainer.getHost(),
                        qpidContainer.getAmqpsPort(),
                        exchangeName
                )),
                "messageType = 'DATEX2'",
                LocalDeliveryStatus.CREATED
        );
        String serviceProviderName = "king_gustaf";
        ServiceProvider king_gustaf = new ServiceProvider(
                serviceProviderName,
                new Capabilities(
                        Collections.singleton(capability)
                ),
                Set.of(new LocalSubscription(
                        LocalSubscriptionStatus.REQUESTED,
                        "messageType = 'DATEX2'",
                        HOST_NAME,
                        Collections.emptySet(),
                        Collections.singleton(new LocalEndpoint(
                                        source,
                                        qpidContainer.getHost(),
                                        qpidContainer.getAmqpsPort()
                                )
                        )
                )),
                Set.of(localDelivery),
                LocalDateTime.now()
        );

        OutgoingMatch outgoingMatch = new OutgoingMatch(
                localDelivery,
                capability,
                serviceProviderName
        );

        serviceProviderRepository.save(king_gustaf);
        outgoingMatchRepository.save(outgoingMatch);

        router.syncServiceProviders(List.of(king_gustaf), client.getQpidDelta());

        SSLContext kingGustafSslContext = sslClientContext(stores, serviceProviderName);
        String amqpsUrl = qpidContainer.getAmqpsUrl();

        Set<LocalEndpoint> sinkEndpoints = king_gustaf.getSubscriptions().stream().flatMap(s -> s.getLocalEndpoints().stream()).collect(Collectors.toSet());
        assertThat(sinkEndpoints).hasSize(1);

        LocalEndpoint endpoint = sinkEndpoints.stream().findFirst().get();
        assertThat(endpoint.getSource()).isEqualTo(source);
        assertThatNoException().isThrownBy(() -> {
            Sink readKingGustafQueue = new Sink(amqpsUrl, source, kingGustafSslContext);
            readKingGustafQueue.start();
        });

        Set<LocalDeliveryEndpoint> deliveryEndpoints = king_gustaf.getDeliveries().stream().flatMap(d -> d.getEndpoints().stream()).collect(Collectors.toSet());
        assertThat(deliveryEndpoints).hasSize(1);
        LocalDeliveryEndpoint deliveryEndpoint = deliveryEndpoints.stream().findFirst().get();
        assertThat(deliveryEndpoint.getTarget()).isEqualTo(exchangeName);

        assertThatNoException().isThrownBy(() -> {
            Source writeDelivery = new Source(amqpsUrl, exchangeName, kingGustafSslContext);
            writeDelivery.start();
        });
        assertThatThrownBy(() -> {
            Sink readDlqueue = new Sink(amqpsUrl, exchangeName, kingGustafSslContext);
            readDlqueue.start();
        } ).isInstanceOf(JMSException.class);
    }


}
