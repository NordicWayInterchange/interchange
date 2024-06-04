package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.SubscriptionMatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {SubscriptionMatchDiscoveryService.class})
public class SubscriptionMatchDiscoveryServiceTest {

    @MockBean
    private SubscriptionMatchRepository subscriptionMatchRepository;

    @Autowired
    private SubscriptionMatchDiscoveryService subscriptionMatchDiscoveryService;

    @Test
    public void noServiceProvidersAndNoNeighboursShouldNotCreateMatches() {
        subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.emptyList(),Collections.emptyList());
        verify(subscriptionMatchRepository,never()).findAllBySubscriptionId(any());
        verify(subscriptionMatchRepository,never()).save(any(SubscriptionMatch.class));

    }

    @Test
    public void noServiceProvidersAndOneNeighbourShouldNotCreateAMatch() {
        subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.emptyList(),Collections.singletonList(new Neighbour(
                "test",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest()
        )));
        verify(subscriptionMatchRepository,never()).findAllBySubscriptionId(any());
        verify(subscriptionMatchRepository,never()).save(any(SubscriptionMatch.class));
    }

    @Test
    public void aServiceProviderAndNoNeighboursShouldNotCreateMatch() {
        subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(
                Collections.singletonList(new ServiceProvider("SP")),
                Collections.emptyList());
        verify(subscriptionMatchRepository,never()).findAllBySubscriptionId(any());
        verify(subscriptionMatchRepository,never()).save(any(SubscriptionMatch.class));
    }

    @Test
    public void serviceProviderAndNeighbourMatches() {
        subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(
                Collections.singletonList(new ServiceProvider(
                        "SP",
                        new Capabilities(),
                        Collections.singleton(
                                new LocalSubscription(
                                        LocalSubscriptionStatus.CREATED,
                                        "originatingCountry = 'NO'",
                                        "my-node"
                                )),
                        Collections.emptySet(),
                        LocalDateTime.now()
                )),
                Collections.singletonList(new Neighbour(
                        "neighbour",
                        new NeighbourCapabilities(),
                        new NeighbourSubscriptionRequest(),
                        new SubscriptionRequest(
                                Collections.singleton(
                                        new Subscription(
                                                "originatingCountry = 'NO'",
                                                SubscriptionStatus.CREATED,
                                                "my-node"
                                        )
                                )
                        ),
                        new Connection()
                ))
        );
        verify(subscriptionMatchRepository,times(1)).findBySubscriptionIdAndAndLocalSubscriptionId(any(), any()); //TODO should check against the actual subscriptionId
        verify(subscriptionMatchRepository,times(1)).save(any(SubscriptionMatch.class));
    }

    //NOTE A localsubscription matching several capabilities at neighbour will only create one subscription on the neighbour.
    @Test
    public void serviceProviderMatchesTwoNeighbourSubscriptions() {
       ServiceProvider sp = new ServiceProvider(
               "SP",
               new Capabilities(),
               Collections.singleton(
                       new LocalSubscription(
                               LocalSubscriptionStatus.CREATED,
                               "originatingCountry = 'NO'",
                               "my-node"
                       )),
               Collections.emptySet(),
               LocalDateTime.now()
       );
       Neighbour neighbour = new Neighbour(
               "neighbour",
               new NeighbourCapabilities(),
               new NeighbourSubscriptionRequest(),
               new SubscriptionRequest(
                       new HashSet<>(Arrays.asList(
                               new Subscription(
                                       "originatingCountry = 'NO'",
                                       SubscriptionStatus.CREATED,
                                       "my-node"
                               )
                       ))
               ),
               new Connection()
       );
        Neighbour otherNeighbour = new Neighbour(
                "neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(
                        new HashSet<>(Arrays.asList(
                                new Subscription(
                                        "originatingCountry = 'NO'",
                                        SubscriptionStatus.CREATED,
                                        "my-node"
                                )
                        ))
                ),
                new Connection()
        );
       subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(
               Collections.singletonList(sp),
               Arrays.asList(neighbour,otherNeighbour)
       );
       verify(subscriptionMatchRepository,times(2)).findBySubscriptionIdAndAndLocalSubscriptionId(any(), any());
       verify(subscriptionMatchRepository,times(2)).save(any(SubscriptionMatch.class));
    }

    @Test
    public void redirectSubscriptionCreatesMatchWithStatusRedirect() {
        ServiceProvider sp = new ServiceProvider(
                "SP",
                new Capabilities(),
                Collections.singleton(
                        new LocalSubscription(
                                LocalSubscriptionStatus.CREATED,
                                "originatingCountry = 'NO'",
                                "SP"
                        )),
                Collections.emptySet(),
                LocalDateTime.now()
        );

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(
                        new HashSet<>(Arrays.asList(
                                new Subscription(
                                        "originatingCountry = 'NO'",
                                        SubscriptionStatus.CREATED,
                                        "SP"
                                )
                        ))
                ),
                new Connection()
        );
        subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(
                Collections.singletonList(sp),
                Collections.singletonList(neighbour)
        );

        verify(subscriptionMatchRepository,times(1)).findBySubscriptionIdAndAndLocalSubscriptionId(any(), any());
        verify(subscriptionMatchRepository,times(1)).save(any(SubscriptionMatch.class));
    }

    @Test
    public void matchIsNotCreatedWhenConsumerCommonNameIsNotTheSameOnLocalSubscriptionAndSubscription() {
        ServiceProvider sp = new ServiceProvider(
                "SP",
                new Capabilities(),
                Collections.singleton(
                        new LocalSubscription(
                                LocalSubscriptionStatus.CREATED,
                                "originatingCountry = 'NO'",
                                "my-node"
                        )),
                Collections.emptySet(),
                LocalDateTime.now()
        );

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new NeighbourCapabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(
                        new HashSet<>(Arrays.asList(
                                new Subscription(
                                        "originatingCountry = 'NO'",
                                        SubscriptionStatus.CREATED,
                                        "SP"
                                )
                        ))
                ),
                new Connection()
        );
        subscriptionMatchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(
                Collections.singletonList(sp),
                Collections.singletonList(neighbour)
        );

        verify(subscriptionMatchRepository,times(0)).findAllBySubscriptionId(any());
        verify(subscriptionMatchRepository,times(0)).save(any(SubscriptionMatch.class));
    }
}
