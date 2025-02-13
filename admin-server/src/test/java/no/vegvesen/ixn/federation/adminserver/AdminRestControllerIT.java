package no.vegvesen.ixn.federation.adminserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = TestApplication.class)
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

    @MockBean
    QpidClient qpidClient;


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
        assertThat(restController.getNeighbours("adminUser")).isNotEmpty();
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
    public void testGetExchanges() throws JsonProcessingException {
        String adminUser = "adminUser";
        String queueName = "outputQueue";

        Exchange exchange = new Exchange(
                "test-exchange",
                "0ba738de-b0ef-4ed8-b3a1-e35c03c18ae0",
                true,
                "headers",
                List.of(new Binding("my-test-binding-key", queueName ,new Filter("a = 'b'")))
        );
        //TODO How to add the exchange to qpidClient?
        //assertThat(restController.getExchanges(adminUser)).isNotEmpty();
        assertThat(exchange.getId()).isEqualTo("0ba738de-b0ef-4ed8-b3a1-e35c03c18ae0");
    }

    @Test
    public void testExchangeExists() {
        when(qpidService.exchangeExists(any())).thenReturn(true);
        assertThat(restController.exchangeExists("adminUser", "exchange")).isTrue();
    }

    @Test
    public void testBindingExists() {
        when(qpidService.bindingExists(any(), any())).thenReturn(true);
        assertThat(restController.bindingExists("adminUser", "exchange", "queue")).isTrue();
    }

}
