package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import jakarta.transaction.Transactional;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class ServiceProviderServiceIT {

    @Autowired
    ServiceProviderRepository repository;

    @Autowired
    NeighbourRepository neighbourRepository;

    @Mock
    OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    ServiceProviderService service;

    @Test
    public void repositoryIsAutowired() {
        assertThat(repository).isNotNull();
    }

    @Test
    public void serviceIsAutowired() {
        assertThat(repository).isNotNull();
    }

    @Test
    public void redirectEndpointsAreSavedFromNeighbour() {
        String serviceProviderName = "my-service-provider";
        String selector = "originatingCountry = 'NO'";
        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, serviceProviderName);

        serviceProvider.addLocalSubscription(localSubscription);

        repository.save(serviceProvider);

        Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, serviceProviderName);
        Endpoint endpoint = new Endpoint("re-queue", "neighbour", 5671);

        subscription.setEndpoints(Collections.singleton(endpoint));

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new Capabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(Collections.singleton(subscription))
        );

        neighbourRepository.save(neighbour);

        Match match = new Match(localSubscription, subscription, serviceProviderName);
        matchRepository.save(match);

        service.syncServiceProviders("my-node", 5671);

        ServiceProvider savedServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).isNotEmpty();
        assertThat(savedServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).hasSize(1);

        service.syncServiceProviders("my-node", 5671);

        ServiceProvider savedAgainServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedAgainServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).hasSize(1);
    }

    @Test
    public void redirectEndpointIsRemovedWhenSubscriptionToNeighbourIsRemoved() {
        String serviceProviderName = "my-service-provider";
        String selector = "originatingCountry = 'NO'";
        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, serviceProviderName);

        serviceProvider.addLocalSubscription(localSubscription);

        repository.save(serviceProvider);

        Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, serviceProviderName);
        Endpoint endpoint = new Endpoint("re-queue", "neighbour", 5671);

        subscription.setEndpoints(Collections.singleton(endpoint));

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new Capabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(Collections.singleton(subscription))
        );

        neighbourRepository.save(neighbour);

        Match match = new Match(localSubscription, subscription, serviceProviderName);
        matchRepository.save(match);

        service.syncServiceProviders("my-node", 5671);

        ServiceProvider savedServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).isNotEmpty();
        assertThat(savedServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).hasSize(1);

        neighbourRepository.deleteAll();
        matchRepository.deleteAll();

        service.syncServiceProviders("my-node", 5671);

        ServiceProvider savedAgainServiceProvider = repository.findByName(serviceProviderName);
        assertThat(savedAgainServiceProvider.getSubscriptions().stream().findFirst().get().getLocalEndpoints()).hasSize(0);
    }
}
