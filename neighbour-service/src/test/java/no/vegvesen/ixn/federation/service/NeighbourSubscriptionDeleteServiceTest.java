package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class NeighbourSubscriptionDeleteServiceTest {

    @Mock
    NeighbourRepository neighbourRepository;

    @Mock
    ListenerEndpointRepository listenerEndpointRepository;

    @Mock
    MatchRepository matchRepository;

    @Mock
    NeighbourFacade neighbourFacade;

    private GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();


    NeighbourSubscriptionDeleteService neighbourSubscriptionDeleteService;

    @BeforeEach
    public void setUp() {
        neighbourSubscriptionDeleteService = new NeighbourSubscriptionDeleteService(neighbourRepository, listenerEndpointRepository, backoffProperties, matchRepository);

    }


    @Test
    public void deleteSubscriptionWhenItHasSubscriptionStatusTear_Down () {
        Neighbour neighbour = new Neighbour();

        Subscription subscription1 = new Subscription(1, SubscriptionStatus.ACCEPTED, "messageType = 'DATEX2' AND originatingCountry = 'NO'", "/neighbour/subscriptions/1", "");
        Subscription subscription2 = new Subscription(2, SubscriptionStatus.ACCEPTED, "messageType = 'DATEX2' AND originatingCountry = 'SE'", "/neighbour/subscriptions/2", "");
        subscription1.setSubscriptionStatus(SubscriptionStatus.ACCEPTED);
        subscription2.setSubscriptionStatus(SubscriptionStatus.TEAR_DOWN);

        SubscriptionRequest existingSubscriptions = new SubscriptionRequest();
        existingSubscriptions.setStatus(SubscriptionRequestStatus.ESTABLISHED);
        existingSubscriptions.setSubscriptions(new HashSet<>(Arrays.asList(subscription1, subscription2)));

        neighbour.setOurRequestedSubscriptions(existingSubscriptions);

        when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.TEAR_DOWN)).thenReturn(Arrays.asList(neighbour));
        when(neighbourRepository.save(neighbour)).thenReturn(neighbour);
        neighbourSubscriptionDeleteService.deleteSubscriptions(neighbourFacade);
        assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);
        assertThat(neighbour.getOurRequestedSubscriptions().getStatus()).isEqualTo(SubscriptionRequestStatus.ESTABLISHED);
    }

    @Test
    public void subscriptionRequestGetStatusEmptyWhenAllSubscriptionsAreDeleted () {
        Neighbour neighbour = new Neighbour();

        Subscription subscription1 = new Subscription(1, SubscriptionStatus.ACCEPTED, "messageType = 'DATEX2' AND originatingCountry = 'NO'", "/neighbour/subscriptions/1", "");
        subscription1.setSubscriptionStatus(SubscriptionStatus.TEAR_DOWN);

        SubscriptionRequest existingSubscriptions = new SubscriptionRequest();
        existingSubscriptions.setStatus(SubscriptionRequestStatus.ESTABLISHED);
        existingSubscriptions.setSubscriptions(Collections.singleton(subscription1));

        neighbour.setOurRequestedSubscriptions(existingSubscriptions);

        when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.TEAR_DOWN)).thenReturn(Arrays.asList(neighbour));
        when(neighbourRepository.save(neighbour)).thenReturn(neighbour);
        neighbourSubscriptionDeleteService.deleteSubscriptions(neighbourFacade);
        assertThat(neighbour.getOurRequestedSubscriptions().getSubscriptions()).hasSize(0);
        assertThat(neighbour.getOurRequestedSubscriptions().getStatus()).isEqualTo(SubscriptionRequestStatus.EMPTY);
    }

    @Test
    public void deleteListenerEndpointWhenThereAreMoreListenerEndpointsThanSubscriptions() {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("neighbour");

        Subscription sub1 = new Subscription(1, SubscriptionStatus.CREATED, "originatingCountry = 'NO'", "/neighbour/subscriptions/1", "");
        Set<Endpoint> endpoints = new HashSet<>();
        Endpoint endpoint = new Endpoint("source-1","endpoint-1", 5671);
        endpoints.add(endpoint);
        sub1.setEndpoints(endpoints);

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setSubscriptions(Collections.singleton(sub1));
        neighbour.setOurRequestedSubscriptions(subscriptionRequest);

        ListenerEndpoint listenerEndpoint1 = new ListenerEndpoint("neighbour", "source-1", "endpoint-1", 5671, new Connection());
        ListenerEndpoint listenerEndpoint2 = new ListenerEndpoint("neighbour", "source-2", "endpoint-2", 5671, new Connection());

        when(listenerEndpointRepository.findAllByNeighbourName("neighbour")).thenReturn(Arrays.asList(listenerEndpoint1, listenerEndpoint2));
        neighbourSubscriptionDeleteService.tearDownListenerEndpoints(neighbour);

        verify(listenerEndpointRepository, times(1)).delete(any(ListenerEndpoint.class));
    }

    @Test
    public void noListenerEndpointsAreRemovedWhenThereAreAsManySubscriptions () {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("neighbour");

        Subscription sub1 = new Subscription(1, SubscriptionStatus.CREATED, "originatingCountry = 'NO'", "/neighbour/subscriptions/1", "");
        Set<Endpoint> endpoints = new HashSet<>();
        Endpoint endpoint = new Endpoint("source-1","endpoint-1", 5671);
        endpoints.add(endpoint);
        sub1.setEndpoints(endpoints);

        Subscription sub2 = new Subscription(2, SubscriptionStatus.CREATED, "originatingCountry = 'SE'", "/neighbour/subscriptions/2", "");
        Set<Endpoint> endpoints2 = new HashSet<>();
        Endpoint endpoint2 = new Endpoint("source-2","endpoint-2", 5671);
        endpoints2.add(endpoint2);
        sub2.setEndpoints(endpoints2);

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setSubscriptions(Sets.newSet(sub1, sub2));
        neighbour.setOurRequestedSubscriptions(subscriptionRequest);

        ListenerEndpoint listenerEndpoint1 = new ListenerEndpoint("neighbour", "source-1", "endpoint-1", 5671, new Connection());
        ListenerEndpoint listenerEndpoint2 = new ListenerEndpoint("neighbour", "source-2", "endpoint-2", 5671, new Connection());

        when(listenerEndpointRepository.findAllByNeighbourName("neighbour")).thenReturn(Arrays.asList(listenerEndpoint1, listenerEndpoint2));
        neighbourSubscriptionDeleteService.tearDownListenerEndpoints(neighbour);

        verify(listenerEndpointRepository, times(0)).delete(any(ListenerEndpoint.class));
    }
}
