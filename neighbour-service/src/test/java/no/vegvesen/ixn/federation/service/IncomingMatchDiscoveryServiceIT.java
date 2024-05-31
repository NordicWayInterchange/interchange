package no.vegvesen.ixn.federation.service;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.federation.model.IncomingMatch;
import no.vegvesen.ixn.federation.model.NeighbourSubscription;
import no.vegvesen.ixn.federation.model.NeighbourSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.repository.IncomingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = PostgresTestcontainerInitializer.Initializer.class)
@Transactional
public class IncomingMatchDiscoveryServiceIT {

    @Autowired
    IncomingMatchRepository incomingMatchRepository;

    @Autowired
    ServiceProviderRepository serviceProviderRepository;

    @Autowired
    IncomingMatchDiscoveryService incomingMatchDiscoveryService;

    @Test
    public void repositoriesAreAutowired(){
        assertThat(incomingMatchRepository).isNotNull();
        assertThat(serviceProviderRepository).isNotNull();
        assertThat(incomingMatchDiscoveryService).isNotNull();
    }

    @Test
    public void testCreatingMatches(){
        NeighbourSubscription neighbourSubscription = new NeighbourSubscription("originatingCountry='NO'", NeighbourSubscriptionStatus.CREATED, "test1");
        Capability capability1 = new Capability(new DatexApplication(), new Metadata());
        Capability capability2 = new Capability(new DenmApplication(), new Metadata());
        Capability capability3 = new Capability(new SsemApplication(), new Metadata());

        incomingMatchDiscoveryService.createMatches(neighbourSubscription, Set.of(capability1, capability2, capability3));
        assertThat(incomingMatchRepository.findAll()).hasSize(3);
    }

    @Test
    public void shouldNotCreateMatchesIfStatusIsNotCreated(){
        NeighbourSubscription neighbourSubscription = new NeighbourSubscription("originatingCountry='NO'", NeighbourSubscriptionStatus.RESUBSCRIBE, "test2");
        Capability capability1 = new Capability(new DatexApplication(), new Metadata());
        Capability capability2 = new Capability(new DenmApplication(), new Metadata());
        Capability capability3 = new Capability(new SsemApplication(), new Metadata());

        incomingMatchDiscoveryService.createMatches(neighbourSubscription, Set.of(capability1, capability2, capability3));
        assertThat(incomingMatchRepository.findAll()).hasSize(0);
    }

    @Test
    public void newMatchExistsReturnsTrueWhenNewMatchIsFound(){
        NeighbourSubscription neighbourSubscription = new NeighbourSubscription("originatingCountry='NO'", NeighbourSubscriptionStatus.CREATED, "test3");
        Capability capability1 = new Capability(new DatexApplication(), new Metadata());
        Capability capability2 = new Capability(new DenmApplication(), new Metadata());
        incomingMatchDiscoveryService.createMatches(neighbourSubscription, Set.of(capability1, capability2));

        ServiceProvider sp = new ServiceProvider();
        Capability capability3 = new Capability(new SsemApplication("publisherId", "publicationId", "NO", "SSEM:1.1", List.of("12002")), new Metadata());
        sp.getCapabilities().setCapabilities(Set.of(capability3));
        serviceProviderRepository.save(sp);

        assertThat(incomingMatchDiscoveryService.newMatchExists(neighbourSubscription)).isTrue();
    }
    @Test
    public void newMatchExistsReturnsFalseWhenNoNewMatchIsFound(){
        NeighbourSubscription neighbourSubscription = new NeighbourSubscription("originatingCountry='NO'", NeighbourSubscriptionStatus.CREATED, "test4");
        Capability capability1 = new Capability(new DatexApplication(), new Metadata());
        Capability capability2 = new Capability(new DenmApplication(), new Metadata());
        incomingMatchDiscoveryService.createMatches(neighbourSubscription, Set.of(capability1, capability2));

        assertThat(incomingMatchDiscoveryService.newMatchExists(neighbourSubscription)).isFalse();
    }

}
