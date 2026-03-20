package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.service.MatchDiscoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = {"no.vegvesen.ixn.federation.repository","no.vegvesen.ixn.federation.model"})
@SpringBootTest(classes = {ServiceProviderRepository.class,NeighbourRepository.class, MatchRepository.class, MatchDiscoveryService.class})
@Transactional
public class MatchDiscoveryServiceIT extends PostgresContainerBase {

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private NeighbourRepository neighbourRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchDiscoveryService matchDiscoveryService;

    @Test
    public void matchDiscovereryServiceIsAutowired() {
        assertThat(matchDiscoveryService).isNotNull();
    }

    @Test
    public void repositoriesAreAutowired() {
        assertThat(matchRepository).isNotNull();
        assertThat(serviceProviderRepository).isNotNull();
        assertThat(neighbourRepository).isNotNull();
    }

    @Test
    public void twoLocalSubscriptionsCanHaveMatchToTheSameSubscription() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        LocalSubscription localSubscription1 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
        LocalSubscription localSubscription2 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider1 = new ServiceProvider("service-provider1", Set.of(localSubscription1));
        serviceProviderRepository.save(serviceProvider1);

        ServiceProvider serviceProvider2 = new ServiceProvider("service-provider2",Set.of(localSubscription2));
        serviceProviderRepository.save(serviceProvider2);

        Subscription subscription = new Subscription(SubscriptionStatus.CREATED, selector, "", consumerCommonName);
        Neighbour neighbour = new Neighbour("neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(
                        Collections.singleton(subscription)
                )
        );
        neighbourRepository.save(neighbour);

        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Arrays.asList(serviceProvider1, serviceProvider2), Collections.singletonList(neighbour));
        assertThat(matchRepository.findAll()).hasSize(2);
    }

    @Test
    public void setupRedirectMatch() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "service-provider";
        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider = new ServiceProvider("service-provider",Set.of(localSubscription));
        serviceProviderRepository.save(serviceProvider);

        Subscription subscription = new Subscription(SubscriptionStatus.CREATED, selector, "", consumerCommonName);
        Neighbour neighbour = new Neighbour("neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(
                        Collections.singleton(subscription)
                )
        );
        neighbourRepository.save(neighbour);
        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.singletonList(serviceProvider), Collections.singletonList(neighbour));

        assertThat(matchRepository.findAll()).hasSize(1);
    }

    @Test
    public void twoLocalSubscriptionsCanHaveMatchToTheSameSubscriptionWhenOneIsAlreadySetUp() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        LocalSubscription localSubscription1 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider1 = new ServiceProvider("service-provider1",Set.of(localSubscription1));
        serviceProviderRepository.save(serviceProvider1);

        Subscription subscription = new Subscription(SubscriptionStatus.CREATED, selector, "", consumerCommonName);
        Neighbour neighbour = new Neighbour("neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(
                        Collections.singleton(subscription)
                )
        );
        neighbourRepository.save(neighbour);

        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.singletonList(serviceProvider1), Collections.singletonList(neighbour));
        assertThat(matchRepository.findAll()).hasSize(1);

        Neighbour savedNeighbour = neighbourRepository.findByName(neighbour.getName());

        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream()
                .findFirst()
                .get();

        assertThat(matchRepository.findAllBySubscriptionId(savedSubscription.getId())).hasSize(1);

        LocalSubscription localSubscription2 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider2 = new ServiceProvider("service-provider2",Set.of(localSubscription2));
        serviceProviderRepository.save(serviceProvider2);

        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Arrays.asList(serviceProvider1, serviceProvider2), Collections.singletonList(neighbour));

        assertThat(matchRepository.findAll()).hasSize(2);
        assertThat(matchRepository.findAllBySubscriptionId(savedSubscription.getId())).hasSize(2);
    }

    @Test
    public void matchIsNotSetUpWhenSubscriptionIsInTearDown() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider = new ServiceProvider("service-provider",Set.of(localSubscription));
        serviceProviderRepository.save(serviceProvider);

        Subscription subscription = new Subscription(SubscriptionStatus.TEAR_DOWN, selector, "", consumerCommonName);
        Neighbour neighbour = new Neighbour("neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(
                        Collections.singleton(subscription)
                )
        );
        neighbourRepository.save(neighbour);

        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.singletonList(serviceProvider), Collections.singletonList(neighbour));
        assertThat(matchRepository.findAll()).hasSize(0);
    }


}
