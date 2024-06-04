package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.SubscriptionMatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import jakarta.transaction.Transactional;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class SubscriptionMatchDiscoveryServiceIT {

    @Autowired
    private SubscriptionMatchRepository subscriptionMatchRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private NeighbourRepository neighbourRepository;

    @Autowired
    private SubscriptionMatchDiscoveryService subscriptionMatchDiscoveryService;

    @Test
    public void matchDiscovereryServiceIsAutowired() {
        assertThat(subscriptionMatchDiscoveryService).isNotNull();
    }

    @Test
    public void repositoriesAreAutowired() {
        assertThat(subscriptionMatchRepository).isNotNull();
        assertThat(serviceProviderRepository).isNotNull();
        assertThat(neighbourRepository).isNotNull();
    }

    @Test
    public void matchIsDeletedIfLocalSubAndSubIsResubscribe(){
        Neighbour nb = new Neighbour();
        Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.RESUBSCRIBE);
        nb.setOurRequestedSubscriptions(new SubscriptionRequest(Set.of(subscription)));
        neighbourRepository.save(nb);

        ServiceProvider sp = new ServiceProvider();
        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.RESUBSCRIBE, "originatingCountry = 'NO'", "test");
        sp.addLocalSubscription(localSubscription);
        serviceProviderRepository.save(sp);

        Match match = new Match(
                localSubscription,
                subscription
        );
        matchRepository.save(match);

        matchDiscoveryService.syncMatchesToDelete();
        List<Match> matches = matchRepository.findAll();
        assertThat(matches).isEmpty();
    }
    @Test
    public void matchIsNotDeletedIfOnlyOneIsResubscribe(){
        Neighbour nb = new Neighbour();
        Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED);
        nb.setOurRequestedSubscriptions(new SubscriptionRequest(Set.of(subscription)));
        neighbourRepository.save(nb);

        ServiceProvider sp = new ServiceProvider();
        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.RESUBSCRIBE, "originatingCountry = 'NO'", "test");
        sp.addLocalSubscription(localSubscription);
        serviceProviderRepository.save(sp);

        Match match = new Match(
                localSubscription,
                subscription
        );
        matchRepository.save(match);

        matchDiscoveryService.syncMatchesToDelete();
        List<Match> matches = matchRepository.findAll();
        assertThat(matches.size()).isEqualTo(1);
    }

    @Test
    public void twoLocalSubscriptionsCanHaveMatchToTheSameSubscription() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        LocalSubscription localSubscription1 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
        LocalSubscription localSubscription2 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider1 = new ServiceProvider("service-provider1");
        serviceProvider1.addLocalSubscription(localSubscription1);
        serviceProviderRepository.save(serviceProvider1);

        ServiceProvider serviceProvider2 = new ServiceProvider("service-provider2");
        serviceProvider2.addLocalSubscription(localSubscription2);
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

        subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Arrays.asList(serviceProvider1, serviceProvider2), Collections.singletonList(neighbour));
        assertThat(subscriptionMatchRepository.findAll()).hasSize(2);
    }

    @Test
    public void setupRedirectMatch() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "service-provider";
        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        serviceProvider.addLocalSubscription(localSubscription);
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
        subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.singletonList(serviceProvider), Collections.singletonList(neighbour));

        assertThat(subscriptionMatchRepository.findAll()).hasSize(1);
    }

    @Test
    public void twoLocalSubscriptionsCanHaveMatchToTheSameSubscriptionWhenOneIsAlreadySetUp() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        LocalSubscription localSubscription1 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider1 = new ServiceProvider("service-provider1");
        serviceProvider1.addLocalSubscription(localSubscription1);
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

        subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.singletonList(serviceProvider1), Collections.singletonList(neighbour));
        assertThat(subscriptionMatchRepository.findAll()).hasSize(1);

        Neighbour savedNeighbour = neighbourRepository.findByName(neighbour.getName());

        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream()
                .findFirst()
                .get();

        assertThat(subscriptionMatchRepository.findAllBySubscriptionId(savedSubscription.getId())).hasSize(1);

        LocalSubscription localSubscription2 = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider2 = new ServiceProvider("service-provider2");
        serviceProvider2.addLocalSubscription(localSubscription2);
        serviceProviderRepository.save(serviceProvider2);

        subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Arrays.asList(serviceProvider1, serviceProvider2), Collections.singletonList(neighbour));

        assertThat(subscriptionMatchRepository.findAll()).hasSize(2);
        assertThat(subscriptionMatchRepository.findAllBySubscriptionId(savedSubscription.getId())).hasSize(2);
    }

    @Test
    public void matchIsNotSetUpWhenSubscriptionIsInTearDown() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "my-node";

        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        serviceProvider.addLocalSubscription(localSubscription);
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

        subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.singletonList(serviceProvider), Collections.singletonList(neighbour));
        assertThat(subscriptionMatchRepository.findAll()).hasSize(0);
    }

}
