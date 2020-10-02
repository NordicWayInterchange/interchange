package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.discoverer.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.service.NeighbourService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessageCollectorTest {

    @Test
    public void testExceptionThrownOnSettingUpConnectionAllowsNextToBeCreated() {
        Neighbour one = new Neighbour("one",new Capabilities(),new SubscriptionRequest(),new SubscriptionRequest(), new ConnectionBackoff());
        Neighbour two = new Neighbour("two",new Capabilities(),new SubscriptionRequest(),new SubscriptionRequest(), new ConnectionBackoff());

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
        NeighbourService neighbourService = mock(NeighbourService.class);
        when(neighbourService.listNeighboursToConsumeMessagesFrom()).thenReturn(Arrays.asList(one,two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(one)).thenThrow(new MessageCollectorException("Expected exception"));

        Source source = mock(Source.class);
        Sink sink = mock(Sink.class);

        when(collectorCreator.setupCollection(two)).thenReturn(new MessageCollectorListener(sink,source));

        MessageCollector collector = new MessageCollector(neighbourService, collectorCreator, backoffProperties);
        collector.runSchedule();

        verify(neighbourService).listNeighboursToConsumeMessagesFrom();
        verify(collectorCreator,times(2)).setupCollection(any());

        assertThat(collector.getListeners()).size().isEqualTo(1);
        assertThat(collector.getListeners()).containsKeys("two");

    }

    @Test
    public void testConnectionsToNeighbourBacksOffWhenNotPossibleToContact(){
        ConnectionBackoff connectionBackoffOne = mock(ConnectionBackoff.class);
        when(connectionBackoffOne.canBeContacted(anyInt(),anyInt())).thenReturn(true);

        ConnectionBackoff connectionBackoffTwo = mock(ConnectionBackoff.class);
        when(connectionBackoffTwo.canBeContacted(anyInt(),anyInt())).thenReturn(true);


        Neighbour one = new Neighbour("one",new Capabilities(),new SubscriptionRequest(),new SubscriptionRequest(), connectionBackoffOne);
        Neighbour two = new Neighbour("two",new Capabilities(),new SubscriptionRequest(),new SubscriptionRequest(),connectionBackoffTwo);

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        NeighbourService neighbourService = mock(NeighbourService.class);
        when(neighbourService.listNeighboursToConsumeMessagesFrom()).thenReturn(Arrays.asList(one,two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(one)).thenThrow(new MessageCollectorException("Expected exception"));

        MessageCollector collector = new MessageCollector(neighbourService, collectorCreator, backoffProperties);
        collector.runSchedule();

        verify(connectionBackoffOne,times(1)).failedConnection(anyInt());
        verify(connectionBackoffTwo,times(1)).okConnection();
    }

}
