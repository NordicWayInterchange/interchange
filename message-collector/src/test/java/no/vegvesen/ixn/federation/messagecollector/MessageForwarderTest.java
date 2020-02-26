package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.naming.NamingException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessageForwarderTest {

    @Test
    public void testExceptionThrownOnSettingUpConnectionAllowsNextToBeCreated() throws NamingException, JMSException {
        Neighbour one = new Neighbour("one",new Capabilities(),new SubscriptionRequest(),new SubscriptionRequest());
        Neighbour two = new Neighbour("two",new Capabilities(),new SubscriptionRequest(),new SubscriptionRequest());

        NeighbourFetcher neighbourFetcher = mock(NeighbourFetcher.class);
        //when(neighbourFetcher.listNeighbourCandidates()).thenReturn(Arrays.asList(one,two));
        when(neighbourFetcher.listNeighboursToConsumeFrom()).thenReturn(Arrays.asList(one,two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        //when(collectorCreator.setupForwarding(one)).thenThrow(new JMSException("Expected exception"));
        when(collectorCreator.setupCollection(one)).thenThrow(new JMSException("Expected exception"));

        MessageProducer producer = mock(MessageProducer.class);
        MessageConsumer consumer = mock(MessageConsumer.class);
        //when(collectorCreator.setupForwarding(two)).thenReturn(new MessageCollectorListener(consumer,producer));
        when(collectorCreator.setupCollection(two)).thenReturn(new MessageCollectorListener(consumer,producer));

        MessageCollector forwarder = new MessageCollector(neighbourFetcher, collectorCreator);
        forwarder.runSchedule();

        //verify(neighbourFetcher).listNeighbourCandidates();
        verify(neighbourFetcher).listNeighboursToConsumeFrom();
        //verify(collectorCreator,times(2)).setupForwarding(any());
        verify(collectorCreator,times(2)).setupCollection(any());

        assertThat(forwarder.getListeners()).size().isEqualTo(1);
        assertThat(forwarder.getListeners()).containsKeys("two");

    }

}
