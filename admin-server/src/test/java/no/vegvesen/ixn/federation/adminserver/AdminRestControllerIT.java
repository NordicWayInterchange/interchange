package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityApi;
import no.vegvesen.ixn.federation.adminserver.qpid.Queue;
import no.vegvesen.ixn.federation.adminserver.qpid.*;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.PathVariableException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
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
    AdminRestController restController;

    @MockBean
    CertService certService;

    @MockBean
    QpidService qpidService;


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
        List<LocalSubscription> subscriptionList = new ArrayList<>();
        LocalSubscription requestedSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "a=b", "my-node");
        LocalSubscription createdSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry='NO", "second-node");
        subscriptionList.add(requestedSubscription);
        subscriptionList.add(createdSubscription);
        ServiceProvider serviceProvider = new ServiceProvider(
                "serviceProvider",
                new Capabilities(),
                subscriptionList,
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


        List<CapabilityApi> response1 = restController.getMatchingSubscriptionCapabilities(adminUser, selector);
        List<CapabilityApi> response2 = restController.getMatchingSubscriptionCapabilities(adminUser, "originatingCountry='SE'");

        assertThat(response1).hasSize(1);
        assertThat(response2).hasSize(0);


        DenmApplication app = new DenmApplication("publisher-1", "publisher-1-0123", "NO", "DENM:1.1.0", List.of("123"), List.of(1));

        Metadata meta =  new Metadata("info.com", 1, RedirectStatus.OPTIONAL, 0, 0, 0);

        Capability cap = new Capability(app, meta);

        LocalSubscription createdSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, "second-node");

        ServiceProvider serviceProvider = new ServiceProvider("sp", new Capabilities(Collections.singleton(cap)), List.of(createdSubscription), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(serviceProvider);

        List<CapabilityApi> response = restController.getMatchingSubscriptionCapabilities(adminUser, selector);

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


        List<CapabilityApi> response1 = restController.getMatchingDeliveryCapabilities(adminUser, actorCommonName, "originatingCountry='SE'");
        List<CapabilityApi> response2 = restController.getMatchingDeliveryCapabilities(adminUser, actorCommonName, selector);
        List<CapabilityApi> response3 = restController.getMatchingDeliveryCapabilities(adminUser, actorCommonName2, selector);

         assertThat(response1).hasSize(0);
         assertThat(response2).hasSize(1);
         assertThat(response3).hasSize(0);

    }
}