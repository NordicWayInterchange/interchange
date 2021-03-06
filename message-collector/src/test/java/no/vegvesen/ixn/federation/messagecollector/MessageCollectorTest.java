package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.model.Connection;
import no.vegvesen.ixn.federation.model.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessageCollectorTest {

    @Test
    public void testExceptionThrownOnSettingUpConnectionAllowsNextToBeCreated() {
        ListenerEndpoint one = new ListenerEndpoint("one", "", "", new Connection());
        ListenerEndpoint two = new ListenerEndpoint("two", "", "", new Connection());

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(one, two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(one)).thenThrow(new MessageCollectorException("Expected exception"));

        Source source = mock(Source.class);
        Sink sink = mock(Sink.class);

        when(collectorCreator.setupCollection(two)).thenReturn(new MessageCollectorListener(sink,source));

        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
        collector.runSchedule();

        verify(collectorCreator,times(2)).setupCollection(any());

        assertThat(collector.getListeners()).size().isEqualTo(1);
        assertThat(collector.getListeners()).containsKeys("two");

    }

    @Test
    public void testConnectionsToNeighbourBacksOffWhenNotPossibleToContact(){
        Connection messageConnectionOne = mock(Connection.class);
        when(messageConnectionOne.canBeContacted(any())).thenReturn(true);

        Connection messageConnectionTwo = mock(Connection.class);
        when(messageConnectionTwo.canBeContacted(any())).thenReturn(true);

        ListenerEndpoint one = new ListenerEndpoint("", "", "", messageConnectionOne);
        ListenerEndpoint two = new ListenerEndpoint("", "", "", messageConnectionTwo);

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(one, two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(one)).thenThrow(new MessageCollectorException("Expected exception"));
        when(collectorCreator.setupCollection(two)).thenReturn(mock(MessageCollectorListener.class));

        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
        collector.runSchedule();

        verify(messageConnectionOne,times(1)).failedConnection(anyInt());
        verify(messageConnectionTwo,times(1)).okConnection();
    }

}
