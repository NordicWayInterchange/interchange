package no.vegvesen.ixn.federation.service.routing.localdelivery;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
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

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@SpringBootTest
@Transactional
public class LocalDeliveryServiceIT {


    public static final String HOST_NAME = QpidDockerBaseIT.getDockerHost();
    private static final ClusterKeyGenerator.CaStores stores = QpidDockerBaseIT.generateStores(QpidDockerBaseIT.getTargetFolderPathForTestClass(LocalDeliveryServiceIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf");

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
    );


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
    RoutingConfigurerProperties routingConfigurerProperties;

    @Autowired
    private OutgoingMatchRepository outgoingMatchRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private LocalDeliveryService localDeliveryService;

    @Autowired
    private QpidClient qpidClient ;


    @Test
    public void createTargetAndConnectForServiceProvider() {
        String serviceProviderName = "my-service-provider";
        String deliveryExchangeName = "my-exchange5";
        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                        Set.of(
                                createLocalDeliveryEndpoint(deliveryExchangeName)
                        ),
                "originatingCountry = 'NO'",
                LocalDeliveryStatus.CREATED,
                "delivery",
                false
        );
        String shardExchange= "cap-ex1";
        qpidClient.createHeadersExchange(shardExchange);
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
                Collections.singletonList(new CapabilityShard(
                        1,
                        shardExchange,
                        "publicationId = 'pub-1'"
                ))
        );
        ServiceProvider serviceProvider = createServiceProvider(serviceProviderName,denmCapability,delivery);

        OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);

        serviceProviderRepository.save(serviceProvider);
        outgoingMatchRepository.save(match);

        localDeliveryService.setUpDeliveryQueue(serviceProvider, qpidClient.getQpidDelta());

        assertThat(qpidClient.exchangeExists(deliveryExchangeName)).isTrue();
        QpidDelta delta = qpidClient.getQpidDelta();
        Exchange deliveryExchange = delta.findByExchangeName(deliveryExchangeName);
        assertThat(deliveryExchange).isNotNull();
        assertThat(deliveryExchange.getBindings().size()).isEqualTo(1);
    }


    @Test
    public void createDlQueueAndConnectForServiceProvider() {
        String serviceProviderName = "my-service-provider";
        String dlqName = "dlq-" + UUID.randomUUID();
        String exchangeName = UUID.randomUUID().toString();
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
                Collections.singletonList(
                        new CapabilityShard(1,
                                exchangeName,
                                "publicationId = 'pub-1'"
                        )
                )
        );
        qpidClient.createHeadersExchange(exchangeName);

        String deliveryExchangeName = "del-" +  UUID.randomUUID();
        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(
                        createLocalDeliveryEndpoint(deliveryExchangeName,dlqName)
                ),
                "originatingCountry = 'NO'",
                LocalDeliveryStatus.CREATED,
                "delivery",
                false
        );

        ServiceProvider serviceProvider = createServiceProvider(serviceProviderName,denmCapability,delivery);

        serviceProviderRepository.save(serviceProvider);
        OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);
        outgoingMatchRepository.save(match);

        localDeliveryService.setUpDeliveryQueue(serviceProvider, qpidClient.getQpidDelta());


        QpidDelta delta = qpidClient.getQpidDelta();
        String actualDlqName = delivery.getEndpoints().stream().findFirst().orElseThrow().getDlqName();
        assertThat(actualDlqName).isEqualTo(dlqName);


        Exchange deliveryExchange = delta.findByExchangeName(deliveryExchangeName);
        assertThat(deliveryExchange).isNotNull();
        assertThat(deliveryExchange.getBindings()).hasSize(1);

        assertThat(qpidClient.getQueue(dlqName)).isNotNull();
    }

    @Test
    public void createTargetAndConnectForServiceProviderWhenCapabilityExchangeDoesNotExist() {
        String serviceProviderName = "my-service-provider";

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
                List.of(
                        new CapabilityShard(
                                1,
                                "cap-non-exist-ex1",
                                "publicationId = 'pub-1'"
                        )
                )
        );


        String deliveryExchangeName = "my-exchange-non-exist5";
        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(
                        createLocalDeliveryEndpoint(deliveryExchangeName)
                ),
                "originatingCountry = 'NO'",
                LocalDeliveryStatus.CREATED,
                "delivery",
                false
        );
        ServiceProvider serviceProvider = createServiceProvider(serviceProviderName,denmCapability,delivery);

        serviceProviderRepository.save(serviceProvider);
        OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);
        outgoingMatchRepository.save(match);

        localDeliveryService.setUpDeliveryQueue(serviceProvider, qpidClient.getQpidDelta());

        assertThat(qpidClient.exchangeExists(deliveryExchangeName)).isTrue();
        QpidDelta qpidDelta = qpidClient.getQpidDelta();
        Exchange deliveryExchange = qpidDelta.findByExchangeName(deliveryExchangeName);
        assertThat(deliveryExchange).isNotNull();
        assertThat(deliveryExchange.getBindings()).hasSize(0);
    }

    @Test
    public void createMultipleTargetsAndConnectForServiceProvider() {
        String serviceProviderName = "my-service-provider";

        String denCapabilityExchange = "cap-ex2";
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
                List.of(
                        new CapabilityShard(
                                1,
                                denCapabilityExchange,
                                "publicationId = 'pub-1'"
                        )
                )
        );
        qpidClient.createHeadersExchange(denCapabilityExchange);

        String denmCapability2Exchange = "cap-ex3";
        Capability denmCapability2 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "1.0",
                        List.of("1234"),
                        List.of(5)
                ),
                new Metadata(RedirectStatus.OPTIONAL),
                List.of(new CapabilityShard(
                                1,
                        denmCapability2Exchange,
                                "publicationId = 'pub-1'"
                        )
                )
        );
        qpidClient.createHeadersExchange(denmCapability2Exchange);

        String deliveryExchangeName = "my-exchange6";
        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(
                        createLocalDeliveryEndpoint(deliveryExchangeName)
                ),
                "originatingCountry = 'NO'",
                LocalDeliveryStatus.CREATED,
                "delivery",
                false
        );
        ServiceProvider serviceProvider = createServiceProvider(
                serviceProviderName,
                Set.of(denmCapability,denmCapability2),
                Set.of(delivery)
        );

        serviceProviderRepository.save(serviceProvider);

        OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);
        OutgoingMatch match2 = new OutgoingMatch(delivery, denmCapability2, serviceProviderName);
        outgoingMatchRepository.save(match);
        outgoingMatchRepository.save(match2);

        localDeliveryService.setUpDeliveryQueue(serviceProvider, qpidClient.getQpidDelta());

        assertThat(qpidClient.exchangeExists(deliveryExchangeName)).isTrue();
        QpidDelta delta = qpidClient.getQpidDelta();
        Exchange deliveryExchange = delta.findByExchangeName(deliveryExchangeName);
        assertThat(deliveryExchange).isNotNull();
        assertThat(deliveryExchange.getBindings()).hasSize(2);
    }

    @Test
    public void tearDownTargetForDeliveryByDeletedCapabilityWhenThereIsNoOtherMatches() {
        String serviceProviderName = "my-service-provider";

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
                List.of(
                        new CapabilityShard(1,
                                "cap-ex5",
                                 "publicationId = 'pub-1'"
                        )
                )
        );
        qpidClient.createHeadersExchange("cap-ex5");

        String deliveryExchangeName = "my-exchange9";
        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(
                        createLocalDeliveryEndpoint(deliveryExchangeName)
                ),
                "originatingCountry = 'NO'",
                LocalDeliveryStatus.CREATED,
                "delivery",
                false
        );

        ServiceProvider serviceProvider = createServiceProvider(serviceProviderName,denmCapability,delivery);
        serviceProviderRepository.save(serviceProvider);

        OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);
        outgoingMatchRepository.save(match);

        localDeliveryService.setUpDeliveryQueue(serviceProvider, qpidClient.getQpidDelta());

        assertThat(qpidClient.exchangeExists(delivery.getEndpoints().stream().findFirst().orElseThrow().getTarget())).isTrue();

        outgoingMatchRepository.delete(match);
        localDeliveryService.tearDownDeliveryQueues(serviceProvider, qpidClient.getQpidDelta());

        assertThat(delivery.getEndpoints()).isEmpty(); //TODO
        assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
        //TODO need to test that the exchanges are gone from qpid, as well
    }

    @Test
    public void tearDownDlqNameAndTargetForDeliveryByDeletedCapabilityWhenThereIsNoOtherMatches() {
        System.out.println(qpidContainer.getHttpUrl());
        String serviceProviderName = "my-service-provider";
        String exchangeName = "dlq1-exchange";
        String dlqName = "dlq-name";

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
                List.of(new CapabilityShard(1, "cap-ex50", "publicationId = 'pub-1'"))
        );
        qpidClient.createHeadersExchange("cap-ex50");

        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(
                        createLocalDeliveryEndpoint(exchangeName,dlqName)
                ),
                "originatingCountry = 'NO'",
                LocalDeliveryStatus.CREATED,
                "delivery",
                false
        );

        ServiceProvider serviceProvider = createServiceProvider(serviceProviderName,denmCapability,delivery);

        serviceProviderRepository.save(serviceProvider);

        OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);
        outgoingMatchRepository.save(match);

        localDeliveryService.setUpDeliveryQueue(serviceProvider, qpidClient.getQpidDelta());

        assertThat(qpidClient.exchangeExists(delivery.getEndpoints().stream().findFirst().orElseThrow().getTarget())).isTrue();
        assertThat(qpidClient.queueExists(delivery.getEndpoints().stream().findFirst().orElseThrow().getDlqName())).isTrue();

        outgoingMatchRepository.delete(match);

        localDeliveryService.tearDownDeliveryQueues(serviceProvider, qpidClient.getQpidDelta());

        assertThat(delivery.getEndpoints()).isEmpty();
        assertThat(qpidClient.queueExists(dlqName)).isFalse();
        assertThat(qpidClient.exchangeExists(exchangeName)).isFalse();
        assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
    }

    @Test
    public void removeOneEndpointsWhenOneOfTwoMatchesIsRemoved() {
        String serviceProviderName = "my-service-provider";

        String cap1ShardExchange = "cap-ex6";
        Capability denmCapability1 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "1.0",
                        List.of("1234"),
                        List.of(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL),
                List.of(
                        new CapabilityShard(
                                1,
                                cap1ShardExchange,
                                "publicationId = 'pub-1'"
                        )
                )
        );
        qpidClient.createHeadersExchange(cap1ShardExchange);

        String cap2ShardExchange = "cap-ex7";
        Capability denmCapability2 = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "1.0",
                        List.of("1233"),
                        List.of(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL),
                List.of(
                        new CapabilityShard(
                                1,
                                cap2ShardExchange,
                                "publicationId = 'pub-1'"
                        )
                )
        );
        qpidClient.createHeadersExchange(cap2ShardExchange);

        String deliveryExchangeName = "my-exchange10";
        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(
                        createLocalDeliveryEndpoint(deliveryExchangeName)
                ),
                "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')",
                LocalDeliveryStatus.CREATED,
                "No delivery",
                false
        );
        ServiceProvider serviceProvider = createServiceProvider(serviceProviderName, Set.of(denmCapability1,denmCapability2),Set.of(delivery));

        serviceProviderRepository.save(serviceProvider);

        OutgoingMatch match1 = new OutgoingMatch(delivery, denmCapability1, serviceProviderName);
        OutgoingMatch match2 = new OutgoingMatch(delivery, denmCapability2, serviceProviderName);
        outgoingMatchRepository.save(match1);
        outgoingMatchRepository.save(match2);

        localDeliveryService.setUpDeliveryQueue(serviceProvider, qpidClient.getQpidDelta());

        assertThat(qpidClient.exchangeExists(delivery.getEndpoints().stream().findFirst().orElseThrow().getTarget())).isTrue();
        //TODO also, check that bindings exist between the the exchanges that each match represents
        outgoingMatchRepository.delete(match1);

        localDeliveryService.tearDownDeliveryQueues(serviceProvider, qpidClient.getQpidDelta());

       assertThat(qpidClient.exchangeExists(delivery.getEndpoints().stream().findFirst().orElseThrow().getTarget())).isTrue();
       assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.CREATED);
       //TODO assert that the bindings that reflect match1 are gone
    }

    @Test
    public void deliveryMatchingShardedCapabilityGetsMultipleBindings() {
        String serviceProviderName = "my-service-provider";

        Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
        String shard1Exchange = "cap-ex12";
        CapabilityShard shard1 = new CapabilityShard(1, shard1Exchange, "publicationId = 'pub-1'");
        qpidClient.createHeadersExchange(shard1Exchange);

        String shard2Exchange= "cap-ex13";
        CapabilityShard shard2 = new CapabilityShard(2, shard2Exchange, "publicationId = 'pub-1'");
        qpidClient.createHeadersExchange(shard2Exchange);

        String shard3Exchange= "cap-ex14";
        CapabilityShard shard3 = new CapabilityShard(3, shard3Exchange, "publicationId = 'pub-1'");
        qpidClient.createHeadersExchange(shard3Exchange);

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
                List.of(
                        shard1,
                        shard2,
                        shard3
                )
        );


        String deliveryExchangeName = "my-exchange11";
        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(
                        createLocalDeliveryEndpoint(deliveryExchangeName)
                ),
                "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')",
                LocalDeliveryStatus.CREATED,
                "Delivery",
                false
        );

        ServiceProvider serviceProvider = createServiceProvider(serviceProviderName, denmCapability,delivery);

        serviceProviderRepository.save(serviceProvider);
        OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);
        outgoingMatchRepository.save(match);

        localDeliveryService.setUpDeliveryQueue(serviceProvider, qpidClient.getQpidDelta());

        assertThat(qpidClient.exchangeExists(deliveryExchangeName)).isTrue();
        QpidDelta delta = qpidClient.getQpidDelta();
        Exchange deliveryExchange = delta.findByExchangeName(deliveryExchangeName);
        assertThat(deliveryExchange).isNotNull();
        assertThat(deliveryExchange.getBindings()).hasSize(3);
    }

    @Test
    public void tearDownDeliveryQueueShouldNotChangeRequestedDeliveries() {
        LocalDelivery localDelivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                "a = b",
                LocalDeliveryStatus.REQUESTED
        );
        ServiceProvider serviceProvider = createServiceProvider("no-change-for-requested-delivery-sp",
                Set.of(),
                Set.of(localDelivery)
        );

        QpidDelta delta = qpidClient.getQpidDelta();

        localDeliveryService.tearDownDeliveryQueues(serviceProvider,delta);
        assertThat(localDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.REQUESTED);
    }

    //TODO the tests below this comment are taken from the old localDeliveryServiceIT, and should be cleaned up.
    //Also, see if we can do things combined in the service (ie in serviceProviderRouter).

    @Test
    public void deliveryReceivesExchangeNameWhenItDoesNotExist(){
        LocalDelivery delivery = new LocalDelivery();
        ServiceProvider serviceProvider = createServiceProvider("service-provider",Set.of(), Set.of(delivery));

        // Will only receive Exchange Name if outgoing match(es) exist
        //TODO is this a reasonable assumption? Should we really have a match with nulls?
        OutgoingMatch outgoingMatch = new OutgoingMatch(delivery, null, serviceProvider.getName());
        outgoingMatchRepository.save(outgoingMatch);

        serviceProviderRepository.save(serviceProvider);
        //TODO this is a weird way of doing things, should we not just create the endpoints straight away?
        localDeliveryService.updateDeliveryStatus("my-interchange", 5671, serviceProvider);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName(serviceProvider.getName());

        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().orElseThrow().getEndpoints()).hasSize(1);
    }

    @Test
    public void deliveryStatusIsSetToNo_OverlapWhenNoMatchesExist(){
        LocalDelivery delivery = new LocalDelivery("originatingCountry='NO'",  LocalDeliveryStatus.CREATED, "Description", false);
        ServiceProvider serviceProvider = createServiceProvider("service-provider", Set.of(), Set.of(delivery));

        serviceProviderRepository.save(serviceProvider);

        localDeliveryService.updateDeliveryStatus("our-node", 5671, serviceProvider);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().orElseThrow().getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
    }

    @Test
    public void deliveryStatusIsSetToNo_OverlapWhenNoMatchesExistAndNoMatchingCapabilitiesExists(){
        LocalDelivery delivery = new LocalDelivery();
        ServiceProvider serviceProvider = createServiceProvider("service-provider", Set.of(),Set.of(delivery));

        serviceProviderRepository.save(serviceProvider);
        localDeliveryService.updateDeliveryStatus("our-node", 5671, serviceProvider);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName(serviceProvider.getName());
        assertThat(savedServiceProvider.getDeliveries().stream().findFirst().orElseThrow().getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
    }

    @Test
    public void deliveryWithErrorGetsRemovedFromServiceProvider(){
        String serviceProviderName = "my-service-provider";
        LocalDelivery delivery = new LocalDelivery("originatingCountry='NO'", LocalDeliveryStatus.ERROR, "description", false);
        ServiceProvider serviceProvider = createServiceProvider(serviceProviderName,  Set.of(), Set.of(delivery));

        serviceProviderRepository.save(serviceProvider);
        localDeliveryService.removeTearDownIllegalAndErrorDeliveries(serviceProvider);

        ServiceProvider savedAgainServiceProvider = serviceProviderRepository.findByName(serviceProviderName);
        assertThat(savedAgainServiceProvider.getDeliveries()).hasSize(0);
    }

    //TODO go through setUpDeliveryQueue and updateDeliveryStatus, and create tests for the different statuses and states we can encounter there.
    //DeliveryStatuses:
    //REQUESTED
    //CREATED
    //NO_OVERLAP

    //TODO what if we create endpoints already in the mapper? That way we can take away most of this code in updateDeliveryStatus
    //This test documents that it is actually a possibility to do that.
    @Test
    public void deliveryWithEndpointEndsUpInTheSameStateAsTheOneWithout() {
        String shardExchange = "cap-" + UUID.randomUUID();
        qpidClient.createHeadersExchange(shardExchange);

        Capability capability = new Capability(
                new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "1.0",
                        List.of("1234"),
                        List.of(6)
                ),
                new Metadata(RedirectStatus.OPTIONAL),
                Collections.singletonList(new CapabilityShard(
                                1,
                                shardExchange,
                                "publicationId = 'pub-1'"
                        )
                )
        );
        ServiceProvider capabilityOwner = createServiceProvider(
                "capabilityOwner",
                Set.of(capability),
                Set.of()
        );
        serviceProviderRepository.save(capabilityOwner);
        LocalDelivery noEndpoints = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(),
                "orignatingCountry = 'NO'",
                LocalDeliveryStatus.REQUESTED,
                "No endpoings",
                false
        );
        ServiceProvider noEndpointOwner = createServiceProvider(
                "noEndpointOwner",
                Set.of(),
                Set.of(
                        noEndpoints
                )
        );
        serviceProviderRepository.save(noEndpointOwner);

        LocalDelivery deliveryWithEndpoint = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(
                        createLocalDeliveryEndpoint("del-" + UUID.randomUUID())
                ),
                "orignatingCountry = 'NO'",
                LocalDeliveryStatus.REQUESTED,
                "Delivery with endpoints",
                false
        );
        ServiceProvider withEndpointOwner = createServiceProvider(
                "withEndpointOwner",
                Set.of(),
                Set.of(deliveryWithEndpoint)
        );
        serviceProviderRepository.save(withEndpointOwner);

        OutgoingMatch noEndpointMatch = new OutgoingMatch(noEndpoints,capability,noEndpointOwner.getName());
        outgoingMatchRepository.save(noEndpointMatch);
        OutgoingMatch deliveryWithEndpointMatch = new OutgoingMatch(deliveryWithEndpoint,capability,withEndpointOwner.getName());
        outgoingMatchRepository.save(deliveryWithEndpointMatch);

        Integer amqpsPort = qpidContainer.getAmqpsPort();

        for (ServiceProvider serviceProvider : List.of(noEndpointOwner,withEndpointOwner)) {
            localDeliveryService.updateDeliveryStatus(HOST_NAME, amqpsPort, serviceProvider);
            localDeliveryService.setUpDeliveryQueue(serviceProvider,qpidClient.getQpidDelta());
            LocalDelivery actualDelivery = serviceProvider.getDeliveries().stream().findFirst().orElseThrow();
            assertThat(actualDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.CREATED);
            assertThat(actualDelivery.getEndpoints()).hasSize(1);
            LocalDeliveryEndpoint actualEndpoints = actualDelivery.getEndpoints().stream().findFirst().orElseThrow();
            assertThat(qpidClient.exchangeExists(actualEndpoints.getTarget())).isTrue();

        }
    }

    private ServiceProvider createServiceProvider(String name, Capability capability, LocalDelivery delivery){
        return createServiceProvider(name, Set.of(capability),Set.of(delivery));
    }

    private ServiceProvider createServiceProvider(String name, Set<Capability> capabilities, Set<LocalDelivery> deliveries){
        return new ServiceProvider(
                name,
                new Capabilities(
                        capabilities
                ),
                Set.of(),
                deliveries,
                LocalDateTime.now()
        );
    }

    private LocalDeliveryEndpoint createLocalDeliveryEndpoint(String target) {
        return new LocalDeliveryEndpoint(
                HOST_NAME,
                qpidContainer.getAmqpsPort(),
                target
        );
    }

    private LocalDeliveryEndpoint createLocalDeliveryEndpoint(String target, String dlq) {
        return new LocalDeliveryEndpoint(
                HOST_NAME,
                qpidContainer.getAmqpsPort(),
                target,
                dlq
        );
    }

}
