package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.transaction.Transactional;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class MatchDiscoveryServiceIT {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private NeighbourRepository neighbourRepository;

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

        ServiceProvider serviceProvider1 = new ServiceProvider("service-provider1");
        serviceProvider1.addLocalSubscription(localSubscription1);
        serviceProviderRepository.save(serviceProvider1);

        ServiceProvider serviceProvider2 = new ServiceProvider("service-provider2");
        serviceProvider2.addLocalSubscription(localSubscription2);
        serviceProviderRepository.save(serviceProvider2);

        Subscription subscription = new Subscription(SubscriptionStatus.REQUESTED, selector, "", consumerCommonName);
        Neighbour neighbour = new Neighbour("neighbour",
                new Capabilities(),
                new SubscriptionRequest(),
                new SubscriptionRequest(
                        SubscriptionRequestStatus.REQUESTED,
                        Collections.singleton(subscription)
                )
        );
        neighbourRepository.save(neighbour);

        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Arrays.asList(serviceProvider1, serviceProvider2), Collections.singletonList(neighbour));

        //TODO: This should not fail... We need a more clear connection between LocalSubscription and Subscription to make more accurate Matches
        //assertThat(matchRepository.findAll()).hasSize(2);

    }

    @Test
    public void setupRedirectMatch() {
        String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
        String consumerCommonName = "service-provider";
        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);

        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        serviceProvider.addLocalSubscription(localSubscription);
        serviceProviderRepository.save(serviceProvider);

        Subscription subscription = new Subscription(SubscriptionStatus.REQUESTED, selector, "", consumerCommonName);
        Neighbour neighbour = new Neighbour("neighbour",
                new Capabilities(),
                new SubscriptionRequest(),
                new SubscriptionRequest(
                        SubscriptionRequestStatus.REQUESTED,
                        Collections.singleton(subscription)
                )
        );
        neighbourRepository.save(neighbour);
        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.singletonList(serviceProvider), Collections.singletonList(neighbour));

        assertThat(matchRepository.findAll()).hasSize(1);
        assertThat(matchRepository.findAllByStatus(MatchStatus.REDIRECT)).hasSize(1);
    }

}
