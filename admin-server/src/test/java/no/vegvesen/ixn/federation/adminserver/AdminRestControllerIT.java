package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = TestApplication.class)
public class AdminRestControllerIT extends PostgresContainerBase {

    @Autowired
    NeighbourRepository neighbourRepository;

    @Autowired
    AdminRestController restController;

    @MockBean
    CertService certService;

    @MockBean
    QpidService qpidService;

    @Test
    public void contextLoads(){}

    @Test
    public void repositoriesAreAutowired(){
        assertThat(neighbourRepository).isNotNull();
        assertThat(restController).isNotNull();
    }
    @Test
    public void testGetNeighbours(){
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
    public void testQueueExists(){
        when(qpidService.queueExists(any())).thenReturn(true);
        assertThat(restController.queueExists("adminUser", "queue")).isTrue();
    }

    @Test
    public void testExchangeExists(){
        when(qpidService.exchangeExists(any())).thenReturn(true);
        assertThat(restController.exchangeExists("adminUser", "exchange")).isTrue();
    }

    @Test
    public void testBindingExists(){
        when(qpidService.bindingExists(any(), any())).thenReturn(true);
        assertThat(restController.bindingExists("adminUser", "exchange", "queue")).isTrue();
    }
}
