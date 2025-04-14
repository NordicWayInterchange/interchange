package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.adminserver.model.privateChannel.PeerPrivateChannelApi;
import no.vegvesen.ixn.federation.adminserver.model.privateChannel.PrivateChannelApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.LocalDeliveryApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.MatchingCapabilityApi;
import no.vegvesen.ixn.federation.adminserver.qpid.*;
import no.vegvesen.ixn.federation.adminserver.qpid.Queue;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.PathVariableException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.util.concurrent.atomic.AtomicInteger;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = {TestApplication.class, MockSslBundle.class})
public class AdminRestControllerIT extends PostgresContainerBase {

    @Autowired
    NeighbourRepository neighbourRepository;

    @Autowired
    ServiceProviderRepository serviceProviderRepository;

    @Autowired
    PrivateChannelRepository privateChannelRepository;

    @Autowired
    AdminRestController restController;

    @MockBean
    CertService certService;

    @MockBean
    QpidService qpidService;

    @MockBean
    AdminQpidClient adminQpidClient;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("KEY_STORE_PASSWORD", () -> "password");
        registry.add("TRUST_STORE_PASSWORD", () -> "password");
    }
    @Test
    public void contextLoads() {
    }

    @Test
    public void repositoriesAreAutowired() {
        assertThat(neighbourRepository).isNotNull();
        assertThat(serviceProviderRepository).isNotNull();
        assertThat(restController).isNotNull();
    }

    @Test
    public void pathVariableWithInvalidCharsThrowsException(){
        assertThrows(PathVariableException.class, () -> restController.getNeighbours("*hal"));
    }

    @Test
    public void testGetNeighbours() {
        String adminUser = "adminUser";
        Neighbour neighbour = new Neighbour(
                "neighbour",
                new NeighbourCapabilities(CapabilitiesStatus.KNOWN,
                        Set.of(
                                new NeighbourCapability(
                                        new DatexApplication("NO12345", "NO12345:dk21o2", "NO", "DATEX2:1.2", List.of("1"), "situationPublication", "bouvet"),
                                        new Metadata("https://www.bouvet.no", 1, RedirectStatus.OPTIONAL, 0, 0, 5)
                                )
                        )),
                new NeighbourSubscriptionRequest(Set.of(
                        new NeighbourSubscription(UUID.randomUUID().toString(), NeighbourSubscriptionStatus.CREATED, "originatingCountry='NO'", "https://path/id", "neighbour", Set.of())
                )),
                new SubscriptionRequest(),
                new Connection()
        );
        neighbourRepository.save(neighbour);
        assertThat(restController.getNeighbours(adminUser)).isNotEmpty();
    }

    @Test
    public void testQueueExists() {
        when(qpidService.queueExists(any())).thenReturn(true);
        assertThat(restController.queueExists("adminUser", "queue")).isTrue();
    }

    @Test
    public void testGetServiceProviders() {
        String adminUser = "adminUser";
        Set<LocalSubscription> subscriptionSet = new HashSet<>();
        LocalSubscription requestedSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "a=b", "my-node");
        LocalSubscription createdSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry='NO", "second-node");
        subscriptionSet.add(requestedSubscription);
        subscriptionSet.add(createdSubscription);
        ServiceProvider serviceProvider = new ServiceProvider(
                "serviceProvider",
                new Capabilities(),
                subscriptionSet,
                Collections.emptySet(),
                LocalDateTime.now()
        );

        serviceProviderRepository.save(serviceProvider);
        assertThat(restController.getServiceProviders(adminUser)).isNotEmpty();
        assertThat(serviceProvider.getSubscriptions().size()).isEqualTo(2);
    }

    @Test
    public void testGetExchanges()  {
        String adminUser = "adminUser";
        String queueName = "outputQueue";

        Exchange exchange = new Exchange(
                "test-exchange",
                "0ba738de-b0ef-4ed8-b3a1-e35c03c18ae0",
                true,
                "headers",
                List.of(new Binding("my-test-binding-key", queueName ,new Filter("a = 'b'")))
        );

        assertThat(exchange.getId()).isEqualTo("0ba738de-b0ef-4ed8-b3a1-e35c03c18ae0");
        when(qpidService.getAllExchanges()).thenReturn(List.of(exchange));
        assertThat(restController.getExchanges(adminUser)).isNotEmpty();
    }

    @Test
    public void testGetQueues()  {
        String adminUser = "adminUser";
        Queue queue = new Queue("1");

        assertThat(queue.getName()).isEqualTo("1");
        when(qpidService.getAllQueues()).thenReturn(List.of(queue));
        assertThat(restController.getQueues(adminUser)).isNotEmpty();
    }

    @Test
    public void testGetMatchingSubscriptionCapabilities() {
        String adminUser = "adminUser";
        String selector = "originatingCountry='NO'";

       Neighbour neighbour = new Neighbour(
                adminUser,
                new NeighbourCapabilities(CapabilitiesStatus.KNOWN,
                        Set.of(
                                new NeighbourCapability(
                                        new DatexApplication("NO12345", "NO12345:dk21o2", "NO", "DATEX2:1.2", List.of("1"),
                                                "situationPublication", "bouvet"),
                                        new Metadata("https://www.bouvet.no", 1, RedirectStatus.OPTIONAL, 0, 0, 5)
                                )
                        )),
                new NeighbourSubscriptionRequest(Set.of(
                        new NeighbourSubscription(UUID.randomUUID().toString(), NeighbourSubscriptionStatus.CREATED, selector, "https://path/id", "neighbour", Set.of())
                )),
                new SubscriptionRequest(),
                new Connection()
        );
        neighbourRepository.save(neighbour);


        List<MatchingCapabilityApi> response1 = restController.getMatchingSubscriptionCapabilities(adminUser, selector);
        List<MatchingCapabilityApi> response2 = restController.getMatchingSubscriptionCapabilities(adminUser, "originatingCountry='SE'");

        assertThat(response1).hasSize(1);
        assertThat(response2).hasSize(0);


        DenmApplication app = new DenmApplication("publisher-1", "publisher-1-0123", "NO", "DENM:1.1.0", List.of("123"), List.of(1));

        Metadata meta =  new Metadata("info.com", 1, RedirectStatus.OPTIONAL, 0, 0, 0);

        Capability cap = new Capability(app, meta);

        LocalSubscription createdSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, "second-node");

        ServiceProvider serviceProvider = new ServiceProvider("sp", new Capabilities(Collections.singleton(cap)), Collections.singleton(createdSubscription), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(serviceProvider);

        List<MatchingCapabilityApi> response = restController.getMatchingSubscriptionCapabilities(adminUser, selector);

        assertThat(response).hasSize(2);
    }

    @Test
    public void testGetMatchingDeliveryCapabilities() {
        String actorCommonName = "actor-1";
        String actorCommonName2 = "actor-2";
        String adminUser = "adminUser";
        String selector = "publicationId='DK12345:publication-id'";

        Capability aCap1 = new Capability(
                new DatexApplication("DK12345","DK12345:publication-id","FI","1", List.of("1"), "type","name"),
                new Metadata("info.com", 1, RedirectStatus.OPTIONAL, 0, 0, 0)
        );

        Capability aCap2 = new Capability(
                new DenmApplication("publisher-1", "publisher-1-0123", "DK", "DENM:1.1.0", List.of("123"), List.of(1)),
                new Metadata("info.com", 1, RedirectStatus.OPTIONAL, 0, 0, 0)
        );


        LocalDelivery aDelivery = new LocalDelivery();
        aDelivery.setSelector(selector);

        LocalDelivery bDelivery = new LocalDelivery();
        bDelivery.setSelector("originatingCountry='SE'");

        ServiceProvider aServiceProvider = new ServiceProvider(actorCommonName);
        aServiceProvider.setCapabilities(new Capabilities(Sets.newLinkedHashSet(aCap1, aCap2), null));
        aServiceProvider.addDeliveries(new HashSet<>(List.of(aDelivery)));
        serviceProviderRepository.save(aServiceProvider);


        List<MatchingCapabilityApi> response1 = restController.getMatchingDeliveryCapabilities(adminUser, actorCommonName, "originatingCountry='SE'");
        List<MatchingCapabilityApi> response2 = restController.getMatchingDeliveryCapabilities(adminUser, actorCommonName, selector);
        assertThatThrownBy(() -> restController.getMatchingDeliveryCapabilities(adminUser, actorCommonName2, selector)).isInstanceOf(NotFoundException.class);

        assertThat(response1).hasSize(0);
        assertThat(response2).hasSize(1);
    }

    @Test
    public void testGetPrivateChannels() {
        String actorCommonName = "actor-1";
        String actorCommonName2 = "actor-2";
        String actorCommonName3 = "actor-3";
        String adminUser = "adminUser";
        privateChannelRepository.save(new PrivateChannel(new HashSet<>(Set.of(new Peer("peerOne"))), PrivateChannelStatus.CREATED, "test",
                new PrivateChannelEndpoint("test", 1337, "test"),
                actorCommonName));

        privateChannelRepository.save(new PrivateChannel(new HashSet<>(Set.of(new Peer("peerTwo"))), PrivateChannelStatus.CREATED, "This is description",
                new PrivateChannelEndpoint("test", 1337, "test"),
                actorCommonName2));

        List<PrivateChannelApi> response1 = restController.getPrivateChannels(adminUser, actorCommonName);
        List<PrivateChannelApi> response2 = restController.getPrivateChannels(adminUser, actorCommonName2);
        List<PrivateChannelApi> response3 = restController.getPrivateChannels(adminUser, actorCommonName3);

        assertThat(response1).hasSize(1);
        assertThat(response2).hasSize(1);
        assertThat(response3).hasSize(0);
    }


    @Test
    public void testGetPrivateChannelsForPeer() {
        String actorCommonName = "actor-1";
        String actorCommonName2 = "actor-2";
        String adminUser = "adminUser";

        PrivateChannel privateChannel1 = new PrivateChannel(new HashSet<>(Set.of(new Peer("PeerOne"))), PrivateChannelStatus.CREATED, "test",
                new PrivateChannelEndpoint("test", 1337, "test"),
                actorCommonName);
        privateChannel1.setLastUpdated(LocalDateTime.now());
        privateChannelRepository.save(privateChannel1);


        PrivateChannel privateChannel2 = new PrivateChannel(new HashSet<>(Set.of(new Peer("PeerTwo"))), PrivateChannelStatus.CREATED, "This is description",
                new PrivateChannelEndpoint("test", 1337, "test"),
                actorCommonName2);

        privateChannel2.setLastUpdated(LocalDateTime.now());
        privateChannelRepository.save(privateChannel2);

        List<PeerPrivateChannelApi> response1 = restController.getPeerPrivateChannels(adminUser, "PeerOne");
        List<PeerPrivateChannelApi> response2 = restController.getPeerPrivateChannels(adminUser, "PeerTwo");

        assertThat(response1).hasSize(1);
        assertThat(response2).hasSize(1);
    }


    @Test void TestGetDeliverysExchangeBindingToMatchingCapability() {

       /* String serviceProviderName = "my-service-provider";
        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

        String selector = "originatingCountry='NO'";

        Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
        metadata.setShardCount(3);
        Capability capability = new Capability(
                new DenmApplication(
                        "NO00000",
                        "NO00000-quad-tree-testing",
                        "NO",
                        "DENM:2.3.2",
                        Collections.singletonList("12003"),
                        Collections.singletonList(6)
                ),
                metadata
        );

        String deliveryExchangeName = "my-exchange11";

        CapabilityShard shard1 = new CapabilityShard(1, "cap-ex12", "publicationId = 'pub-1'");
        adminQpidClient.createHeadersExchange(deliveryExchangeName);

        CapabilityShard shard2 = new CapabilityShard(2, "cap-ex13", "publicationId = 'pub-1'");
        adminQpidClient.createHeadersExchange(deliveryExchangeName);

        CapabilityShard shard3 = new CapabilityShard(3, "cap-ex14", "publicationId = 'pub-1'");
        adminQpidClient.createHeadersExchange("cap-ex14");

        capability.setShards(Arrays.asList(shard1, shard2, shard3));

        assertThat(CapabilityMatcher.matchCapabilitiesToSelector(Collections.singleton(capability), selector)).hasSize(1);


        LocalDelivery bDelivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED, "Delivery");
        bDelivery.addEndpoint(new LocalDeliveryEndpoint("my-interchange", 5671, deliveryExchangeName));
        bDelivery.setStatus(LocalDeliveryStatus.CREATED);


        ServiceProvider aServiceProvider = new ServiceProvider(serviceProviderName);
        aServiceProvider.setCapabilities(new Capabilities(Sets.newLinkedHashSet(capability), null));
        aServiceProvider.addDeliveries(new HashSet<>(List.of(bDelivery)));
        serviceProviderRepository.save(aServiceProvider);

        System.out.println(aServiceProvider.getCapabilities());
        System.out.println(aServiceProvider.getDeliveries());

        CapabilityApi response1 = restController.getDeliverysExchangeBindingToMatchingCapability("adminUser", serviceProviderName, bDelivery.getUuid());
        assertThat(response1).isNotNull();*/

        String exchangeName = "intermediate-exchange";
        String inQueueName = "delivery-exchange";
        String outQueueName = "king_gustaf";

        Subscription subscription = new Subscription(
                "originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = 6",
                SubscriptionStatus.CREATED
        );

        Capability capability = new Capability(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("12004"),
                        List.of(6)
                ),
                new Metadata()
        );

        LocalDelivery delivery = new LocalDelivery(
                "originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = 6",
                LocalDeliveryStatus.CREATED,
                "DENM delivery"
        );

        adminQpidClient.createDirectExchange(inQueueName);

        adminQpidClient.createQueue(outQueueName);

        adminQpidClient.createHeadersExchange(exchangeName);

        String capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability, null);
        System.out.println(capabilitySelector);

        String deliverySelector = delivery.getSelector();

        String subscriptionSelector = subscription.getSelector();

        String joinedSelector = String.format("(%s) AND (%s)", capabilitySelector, deliverySelector);
        System.out.println(joinedSelector);

        adminQpidClient.addBinding(inQueueName, new Binding(inQueueName, exchangeName, new Filter(joinedSelector)));
        adminQpidClient.addBinding(exchangeName, new Binding(exchangeName, outQueueName, new Filter(subscriptionSelector)));

        AtomicInteger numMessages = new AtomicInteger();

        assertThat(numMessages.get()).isEqualTo(1);


    }

   /* @Test
    public void testGetDeliveriesForEachServiceProvider() {
        String actorCommonName = "sp-1";
        String actorCommonName2 = "sp-2";
        String adminUser = "adminUser";
        String selector = "publicationId='DK12345'";

        Capability aCap1 = new Capability(
                new DatexApplication("DK12345","DK12345","FI","1", List.of("1"), "type","name"),
                new Metadata("info.com", 1, RedirectStatus.OPTIONAL, 0, 0, 0)
        );

        LocalDelivery aDelivery = new LocalDelivery();
        aDelivery.setSelector(selector);

        LocalDelivery bDelivery = new LocalDelivery();
        bDelivery.setSelector("originatingCountry='SE'");


        ServiceProvider bServiceProvider = new ServiceProvider(actorCommonName);
        bServiceProvider.setCapabilities(new Capabilities(Sets.newLinkedHashSet(aCap1), null));
        bServiceProvider.addDeliveries(new HashSet<>(List.of(aDelivery, bDelivery)));
        serviceProviderRepository.save(bServiceProvider);


        List<LocalDeliveryApi> response1 = restController.getDeliveriesForEachServiceProvider(adminUser, actorCommonName);
        List<LocalDeliveryApi> response2 = restController.getDeliveriesForEachServiceProvider(adminUser, actorCommonName2);

        assertThat(response1).hasSize(2);
        assertThat(response2).hasSize(0);
    }*/

    @Test
    public void testGetDeliverysExchangeBindingToMatchingCapabilities() {
        String serviceProviderName = "my-service-provider";
        String adminUser = "adminUser";
        Capability aCap1 = new Capability(
                new DatexApplication("DK12345","DK12345:publication-id","NO","1", List.of("1"), "type","name"),
                new Metadata()
        );

        CapabilityShard shard = new CapabilityShard(1, "cap-ex3", "publicationId = 'pub-1'");
        aCap1.setShards(Collections.singletonList(shard));

        LocalDelivery aDelivery = new LocalDelivery();

        ServiceProvider aServiceProvider = new ServiceProvider(serviceProviderName);
        serviceProviderRepository.save(aServiceProvider);

        List<CapabilitiesLinkedDeliveryApi> capabilitiesLinkedDeliveryApiList = new ArrayList<>();

        CapabilityMatchApi capabilityMatchApi = new CapabilityMatchApi(
                aCap1.getUuid(),
                1,
                new Binding("exchange", "queueName", new Filter("publicationId = 'pub-1'"))
        );

        capabilitiesLinkedDeliveryApiList.add(new CapabilitiesLinkedDeliveryApi(aDelivery.getUuid(), capabilityMatchApi));
        when(qpidService.getCapabilitiesLinkedDelivery(aServiceProvider, aDelivery.getUuid())).thenReturn(capabilitiesLinkedDeliveryApiList);
        assertThat(restController.getDeliverysExchangeBindingToMatchingCapabilities(adminUser, serviceProviderName, aDelivery.getUuid())).isNotEmpty();
    }
}