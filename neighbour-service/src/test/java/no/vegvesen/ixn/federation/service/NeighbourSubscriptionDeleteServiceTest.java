package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NeighbourSubscriptionDeleteServiceTest {

    @Mock
    NeighbourRepository neighbourRepository;

    @Mock
    MatchRepository matchRepository;

    @Mock
    NeighbourFacade neighbourFacade;

    private final GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();


    NeighbourSubscriptionDeleteService neighbourSubscriptionDeleteService;

    @BeforeEach
    public void setUp() {
        neighbourSubscriptionDeleteService = new NeighbourSubscriptionDeleteService(neighbourRepository, backoffProperties, matchRepository);

    }


    @Test
    public void deleteSubscriptionWhenItHasSubscriptionStatusTear_Down () {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("neighbour");

        Subscription subscription1 = new Subscription(1, SubscriptionStatus.REQUESTED, "messageType = 'DATEX2' AND originatingCountry = 'NO'", "/neighbour/subscriptions/1", "");
        Subscription subscription2 = new Subscription(2, SubscriptionStatus.TEAR_DOWN, "messageType = 'DATEX2' AND originatingCountry = 'SE'", "/neighbour/subscriptions/2", "");

        SubscriptionRequest existingSubscriptions = new SubscriptionRequest();
        existingSubscriptions.setSubscriptions(new HashSet<>(Arrays.asList(subscription1, subscription2)));

        neighbour.setOurRequestedSubscriptions(existingSubscriptions);

        when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.TEAR_DOWN)).thenReturn(List.of(neighbour));
        when(neighbourRepository.save(neighbour)).thenReturn(neighbour);
        neighbourSubscriptionDeleteService.deleteSubscriptions(neighbourFacade);
        assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);
    }

    @Test
    public void subscriptionRequestGetStatusEmptyWhenAllSubscriptionsAreDeleted () {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("neighbour");

        Subscription subscription1 = new Subscription(1, SubscriptionStatus.TEAR_DOWN, "messageType = 'DATEX2' AND originatingCountry = 'NO'", "/neighbour/subscriptions/1", "");

        SubscriptionRequest existingSubscriptions = new SubscriptionRequest();
        existingSubscriptions.setSubscriptions(Collections.singleton(subscription1));

        neighbour.setOurRequestedSubscriptions(existingSubscriptions);

        when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.TEAR_DOWN)).thenReturn(List.of(neighbour));
        when(neighbourRepository.save(neighbour)).thenReturn(neighbour);
        neighbourSubscriptionDeleteService.deleteSubscriptions(neighbourFacade);
        assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptions()).hasSize(0);
    }

    @Test
    public void deleteReturns404FromNeighbour() {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("neighbour");

        Subscription subscription1 = new Subscription(1, SubscriptionStatus.TEAR_DOWN, "messageType = 'DATEX2' AND originatingCountry = 'NO'", "/neighbour/subscriptions/1", "");

        SubscriptionRequest existingSubscriptions = new SubscriptionRequest();
        existingSubscriptions.setSubscriptions(Collections.singleton(subscription1));

        neighbour.setOurRequestedSubscriptions(existingSubscriptions);

        when(neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.TEAR_DOWN)).thenReturn(List.of(neighbour));
        when(neighbourRepository.save(neighbour)).thenReturn(neighbour);
        doThrow(SubscriptionNotFoundException.class).when(neighbourFacade).deleteSubscription(neighbour,subscription1);
        neighbourSubscriptionDeleteService.deleteSubscriptions(neighbourFacade);
        assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptions()).isEmpty();
    }
}
