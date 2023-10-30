package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessageCollectorTest {

    @Test
    public void testTwoListenersAreCreatedForOneNeighbourWithTwoEndpoints() {
        Subscription sub1 = new Subscription(
                "messageType = 'DENM' AND originatingCountry = 'NO'",
                SubscriptionStatus.CREATED
        );
        sub1.setEndpoints(Collections.singleton(new Endpoint("source-1", "host", 5671, new SubscriptionShard("subscriptionExchange1"))));

        Subscription sub2 = new Subscription(
                "messageType = 'DENM' AND originatingCountry = 'SE'",
                SubscriptionStatus.CREATED
        );
        sub2.setEndpoints(Collections.singleton(new Endpoint("source-2", "host", 5671, new SubscriptionShard("subscriptionExchange2"))));

        Neighbour neighbour = new Neighbour(
                "neighbour",
                new Capabilities(),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest(new HashSet<>(Arrays.asList(sub1, sub2)))
        );

        NeighbourRepository neighbourRepository = mock(NeighbourRepository.class);
        when(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(any())).thenReturn(Collections.singletonList(neighbour));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(any())).thenReturn(mock(MessageCollectorListener.class));
        when(collectorCreator.setupCollection(any())).thenReturn(mock(MessageCollectorListener.class));

        MessageCollector collector = new MessageCollector(neighbourRepository, collectorCreator);
        collector.runSchedule();

        assertThat(collector.getListeners()).size().isEqualTo(2);
    }

    @Test
    public void removeStoppedListener() {
        NeighbourRepository neighbourRepository = mock(NeighbourRepository.class);
        CollectorCreator collectorCreator = mock(CollectorCreator.class);

        HashMap<Endpoint, MessageCollectorListener> listeners = new HashMap<>();
        MessageCollectorListener listener = mock(MessageCollectorListener.class);
        when(listener.isRunning()).thenReturn(false);
        Endpoint one = new Endpoint("source-one", "host-one", 5671, new SubscriptionShard("subscriptionExchange1"));
        listeners.put(one, listener);

        MessageCollector messageCollector = new MessageCollector(neighbourRepository,collectorCreator, listeners);

        messageCollector.checkListenerList();
        assertThat(listeners).isEmpty();
    }
}
