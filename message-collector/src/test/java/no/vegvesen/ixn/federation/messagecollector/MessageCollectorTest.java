package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessageCollectorTest {

    @Test
    public void testExceptionThrownOnSettingUpConnectionAllowsNextToBeCreated() {
        ListenerEndpoint one = new ListenerEndpoint("one", "", "", 5671,  new Connection(), "subscriptionExchange1");
        ListenerEndpoint two = new ListenerEndpoint("two", "", "", 5671,  new Connection(), "subscriptionExchange2");

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(one, two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(eq(one))).thenThrow(new MessageCollectorException("Expected exception"));

        Source source = mock(Source.class);
        Sink sink = mock(Sink.class);

        when(collectorCreator.setupCollection(eq(two))).thenReturn(new MessageCollectorListener(sink,source));

        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
        collector.runSchedule();

        verify(collectorCreator,times(2)).setupCollection(any());

        assertThat(collector.getListeners()).size().isEqualTo(1);
        assertThat(collector.getListeners()).containsKeys(two);

    }

    @Test
    public void testConnectionsToNeighbourBacksOffWhenNotPossibleToContact(){
        Connection messageConnectionOne = mock(Connection.class);
        when(messageConnectionOne.canBeContacted(any())).thenReturn(true);

        Connection messageConnectionTwo = mock(Connection.class);
        when(messageConnectionTwo.canBeContacted(any())).thenReturn(true);

        ListenerEndpoint one = new ListenerEndpoint("one", "source-one", "endpoint-one", 5671,  messageConnectionOne, "subscriptionExchange1");
        ListenerEndpoint two = new ListenerEndpoint("two", "source-two", "endpoint-two", 5671,  messageConnectionTwo, "subscriptionExchange2");

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(one, two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(eq(one))).thenThrow(new MessageCollectorException("Expected exception"));
        when(collectorCreator.setupCollection(eq(two))).thenReturn(mock(MessageCollectorListener.class));

        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
        collector.runSchedule();

        verify(messageConnectionOne,times(1)).failedConnection(anyInt());
        verify(messageConnectionTwo,times(1)).okConnection();
    }

    @Test
    public void testTwoListenersAreCreatedForOneNeighbourWithTwoEndpoints() {
        Connection messageConnectionOne = mock(Connection.class);
        when(messageConnectionOne.canBeContacted(any())).thenReturn(true);

        Connection messageConnectionTwo = mock(Connection.class);
        when(messageConnectionTwo.canBeContacted(any())).thenReturn(true);

        ListenerEndpoint one = new ListenerEndpoint("one", "source-one", "endpoint-one", 5671, messageConnectionOne, "subscriptionExchange1");
        ListenerEndpoint two = new ListenerEndpoint("one", "source-two", "endpoint-two", 5671, messageConnectionTwo, "subscriptionExchange2");

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(one, two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(eq(one))).thenReturn(mock(MessageCollectorListener.class));
        when(collectorCreator.setupCollection(eq(two))).thenReturn(mock(MessageCollectorListener.class));

        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
        collector.runSchedule();

        assertThat(collector.getListeners()).size().isEqualTo(2);
        verify(messageConnectionOne,times(1)).okConnection();
        verify(messageConnectionTwo,times(1)).okConnection();
    }

    @Test
    public void testOneListenerIsCreatedForOneNeighbourWithTwoEndpointsOneFailing() {
        Connection messageConnectionOne = mock(Connection.class);
        when(messageConnectionOne.canBeContacted(any())).thenReturn(true);

        Connection messageConnectionTwo = mock(Connection.class);
        when(messageConnectionTwo.canBeContacted(any())).thenReturn(true);

        ListenerEndpoint one = new ListenerEndpoint("one", "source-one", "endpoint-one", 5671, messageConnectionOne, "subscriptionExchange1");
        ListenerEndpoint two = new ListenerEndpoint("one", "source-two", "endpoint-two", 5671, messageConnectionTwo, "subscriptionExchange2");

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(one, two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(eq(one))).thenThrow(new MessageCollectorException("Expected exception"));
        when(collectorCreator.setupCollection(eq(two))).thenReturn(mock(MessageCollectorListener.class));

        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
        collector.runSchedule();

        assertThat(collector.getListeners()).size().isEqualTo(1);
        verify(messageConnectionOne,times(1)).failedConnection(anyInt());
        verify(messageConnectionTwo,times(1)).okConnection();
    }

    @Test
    public void matchWithNoListenerEndpointIsSetToTearDownExchange() {
        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Collections.emptyList());

        CollectorCreator collectorCreator = mock(CollectorCreator.class);

        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
        collector.runSchedule();

        assertThat(collector.getListeners()).size().isEqualTo(0);
    }

    @Test
    public void twoMatchesOneWithListenerEndpointAndOneWithoutTearDownOne() {
        Connection messageConnectionOne = mock(Connection.class);
        when(messageConnectionOne.canBeContacted(any())).thenReturn(true);

        ListenerEndpoint one = new ListenerEndpoint("one", "source-one", "endpoint-one", 5671, messageConnectionOne, "subscriptionExchange1");

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Collections.singletonList(one));

        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(eq(one))).thenReturn(mock(MessageCollectorListener.class));

        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
        collector.runSchedule();

        assertThat(collector.getListeners()).size().isEqualTo(1);
        verify(messageConnectionOne,times(1)).okConnection();
    }

    @Test
    public void removeStoppedListener() {
        Connection messageConnectionOne = new Connection();
        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        HashMap<ListenerEndpoint, MessageCollectorListener> listeners = new HashMap<>();
        MessageCollectorListener listener = mock(MessageCollectorListener.class);
        when(listener.isRunning()).thenReturn(false);
        ListenerEndpoint one = new ListenerEndpoint("one", "source-one", "endpoint-one", 5671, messageConnectionOne, "subscriptionExchange1");
        listeners.put(one, listener);

        MessageCollector messageCollector = new MessageCollector(listenerEndpointRepository,collectorCreator,backoffProperties, listeners);

        messageCollector.checkListenerList();
        assertThat(listeners).isEmpty();



    }
}
