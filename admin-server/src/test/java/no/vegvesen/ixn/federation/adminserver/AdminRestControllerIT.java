package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


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
    public void testgetServiceProviders() {
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
}
