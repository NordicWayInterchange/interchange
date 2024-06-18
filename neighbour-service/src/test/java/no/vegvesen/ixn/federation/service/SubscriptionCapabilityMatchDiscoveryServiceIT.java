package no.vegvesen.ixn.federation.service;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.repository.SubscriptionCapabilityMatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = PostgresTestcontainerInitializer.Initializer.class)
@Transactional
public class SubscriptionCapabilityMatchDiscoveryServiceIT {

    @Autowired
    SubscriptionCapabilityMatchRepository subscriptionCapabilityMatchRepository;

    @Autowired
    ServiceProviderRepository serviceProviderRepository;

    @Autowired
    SubscriptionCapabilityMatchDiscoveryService subscriptionCapabilityMatchDiscoveryService;

    @Autowired
    NeighbourRepository neighbourRepository;

    @Test
    public void repositoriesAreAutowired(){
        assertThat(subscriptionCapabilityMatchRepository).isNotNull();
        assertThat(serviceProviderRepository).isNotNull();
        assertThat(subscriptionCapabilityMatchDiscoveryService).isNotNull();
    }

    @Test
    public void testCreatingMatches(){
        Neighbour neighbour = new Neighbour();
        NeighbourSubscription neighbourSubscription = new NeighbourSubscription("originatingCountry='NO'", NeighbourSubscriptionStatus.CREATED, "test1");
        neighbour.setNeighbourRequestedSubscriptions(new NeighbourSubscriptionRequest(Set.of(neighbourSubscription)));
        neighbourRepository.save(neighbour);

        ServiceProvider sp = new ServiceProvider();
        Capability capability1 = new Capability(new DatexApplication(), new Metadata());
        Capability capability2 = new Capability(new DenmApplication(), new Metadata());
        Capability capability3 = new Capability(new SsemApplication(), new Metadata());
        sp.getCapabilities().setCapabilities(Set.of(capability1, capability2, capability3));
        serviceProviderRepository.save(sp);

        subscriptionCapabilityMatchDiscoveryService.createMatch(neighbourSubscription, capability1);
        subscriptionCapabilityMatchDiscoveryService.createMatch(neighbourSubscription, capability2);
        subscriptionCapabilityMatchDiscoveryService.createMatch(neighbourSubscription, capability3);

        assertThat(subscriptionCapabilityMatchRepository.findAll()).hasSize(3);
    }

    @Test
    public void newMatchExistsReturnsTrueWhenNewMatchIsFound(){
        Neighbour neighbour = new Neighbour();
        NeighbourSubscription neighbourSubscription = new NeighbourSubscription("originatingCountry='NO'", NeighbourSubscriptionStatus.CREATED, "test3");
        neighbour.setNeighbourRequestedSubscriptions(new NeighbourSubscriptionRequest(Set.of(neighbourSubscription)));
        neighbourRepository.save(neighbour);

        ServiceProvider sp = new ServiceProvider("test");
        Capability capability1 = new Capability(new DatexApplication(), new Metadata());
        Capability capability2 = new Capability(new DenmApplication(), new Metadata());
        sp.getCapabilities().setCapabilities(Set.of(capability1, capability2));
        serviceProviderRepository.save(sp);

        subscriptionCapabilityMatchDiscoveryService.createMatch(neighbourSubscription, capability1);
        subscriptionCapabilityMatchDiscoveryService.createMatch(neighbourSubscription, capability2);

        ServiceProvider sp2 = new ServiceProvider();
        Capability capability3 = new Capability(new SsemApplication("publisherId", "publicationId", "NO", "SSEM:1.1", List.of("12002")), new Metadata());
        sp2.getCapabilities().setCapabilities(Set.of(capability3));
        serviceProviderRepository.save(sp2);

        assertThat(subscriptionCapabilityMatchDiscoveryService.newMatchExists(neighbourSubscription, sp2.getCapabilities().getCapabilities())).isTrue();
    }

    @Test
    public void newMatchExistsReturnsFalseWhenNoNewMatchIsFound(){
        Neighbour neighbour = new Neighbour();
        NeighbourSubscription neighbourSubscription = new NeighbourSubscription("originatingCountry='FI'", NeighbourSubscriptionStatus.CREATED, "test4");
        neighbour.setNeighbourRequestedSubscriptions(new NeighbourSubscriptionRequest(Set.of(neighbourSubscription)));
        neighbourRepository.save(neighbour);

        ServiceProvider sp = new ServiceProvider("test2");
        Capability capability1 = new Capability(new DatexApplication(), new Metadata());
        Capability capability2 = new Capability(new DenmApplication(), new Metadata());
        sp.getCapabilities().setCapabilities(Set.of(capability1, capability2));
        sp = serviceProviderRepository.save(sp);

        subscriptionCapabilityMatchDiscoveryService.createMatch(neighbourSubscription, capability1);
        subscriptionCapabilityMatchDiscoveryService.createMatch(neighbourSubscription, capability2);


        assertThat(subscriptionCapabilityMatchDiscoveryService.newMatchExists(neighbourSubscription, sp.getCapabilities().getCapabilities())).isFalse();
    }

}
