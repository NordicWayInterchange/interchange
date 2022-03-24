package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
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

@SpringBootTest(classes = {MatchDiscoveryService.class})
public class MatchDiscoveryServiceTest {

    @MockBean
    private MatchRepository matchRepository;

    @Autowired
    private MatchDiscoveryService matchDiscoveryService;

    @Test
    public void noServiceProvidersAndNoNeighboursShouldNotCreateMatches() {
        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.emptyList(),Collections.emptyList());
        verify(matchRepository,never()).findBySubscriptionId(any());
        verify(matchRepository,never()).save(any(Match.class));

    }

    @Test
    public void noServiceProvidersAndOneNeighbourShouldNotCreateAMatch() {
        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(Collections.emptyList(),Collections.singletonList(new Neighbour(
                "test",
                new Capabilities(),
                new SubscriptionRequest(),
                new SubscriptionRequest()
        )));
        verify(matchRepository,never()).findBySubscriptionId(any());
        verify(matchRepository,never()).save(any(Match.class));
    }

    @Test
    public void aServiceProviderAndNoNeighboursShouldNotCreateMatch() {
        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(
                Collections.singletonList(new ServiceProvider("SP")),
                Collections.emptyList());
        verify(matchRepository,never()).findBySubscriptionId(any());
        verify(matchRepository,never()).save(any(Match.class));
    }

    @Test
    public void serviceProviderAndNeighbourMatches() {
        matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(
                Collections.singletonList(new ServiceProvider(
                        "SP",
                        new Capabilities(),
                        Collections.singleton(
                                new LocalSubscription(
                                        LocalSubscriptionStatus.REQUESTED,
                                        "originatingCountry = 'NO'"
                                )),
                        Collections.emptySet(),
                        LocalDateTime.now()
                )),
                Collections.singletonList(new Neighbour(
                        "neighbour",
                        new Capabilities(),
                        new SubscriptionRequest(),
                        new SubscriptionRequest(
                                SubscriptionRequestStatus.REQUESTED,
                                Collections.singleton(
                                        new Subscription(
                                                "originatingCountry = 'NO'",
                                                SubscriptionStatus.REQUESTED
                                        )
                                )
                        ),
                        new Connection()
                ))
        );
        verify(matchRepository,times(1)).findBySubscriptionId(any()); //TODO should check against the actual subscriptionId
        verify(matchRepository,times(1)).save(any(Match.class));
    }

    //TODO one LocalSubscription, two matching Subscriptions
    @Test
    public void serviceProviderMatchesTwoSubscriptionForOneNeighbour() {
       ServiceProvider sp = new ServiceProvider(
               "SP",
               new Capabilities(),
               Collections.singleton(
                       new LocalSubscription(
                               LocalSubscriptionStatus.REQUESTED,
                               "originatingCountry = 'NO'"
                       )),
               Collections.emptySet(),
               LocalDateTime.now()
       );
       Neighbour neighbour = new Neighbour(
               "neighbour",
               new Capabilities(),
               new SubscriptionRequest(),
               new SubscriptionRequest(
                       SubscriptionRequestStatus.REQUESTED,
                       new HashSet<>(Arrays.asList(
                               new Subscription(
                                       "originatingCountry = 'NO'",
                                       SubscriptionStatus.REQUESTED
                               ),
                               new Subscription(
                                       //TODO How the hell do we match to create the darned subscriptions?

                               )
                       ))
               ),
               new Connection()
       );
    }




    //TODO several subscriptions, one REQUESTED, or none REQUESTED
}
