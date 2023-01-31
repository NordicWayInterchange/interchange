package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.service.MatchDiscoveryService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessageCollectorTest {

    @Test
    public void testExceptionThrownOnSettingUpConnectionAllowsNextToBeCreated() {
        ListenerEndpoint one = new ListenerEndpoint("one", "", "", 5671,  new MessageConnection(), "subscriptionExchange1");
        ListenerEndpoint two = new ListenerEndpoint("two", "", "", 5671,  new MessageConnection(), "subscriptionExchange2");

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(one, two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(eq(one))).thenThrow(new MessageCollectorException("Expected exception"));

        Source source = mock(Source.class);
        Sink sink = mock(Sink.class);

        when(collectorCreator.setupCollection(eq(two))).thenReturn(new MessageCollectorListener(sink,source));

        MatchRepository matchRepository = mock(MatchRepository.class);
        MatchDiscoveryService matchDiscoveryService = new MatchDiscoveryService(matchRepository);
        Match match1 = new Match(new LocalSubscription(), new Subscription(), MatchStatus.SETUP_ENDPOINT);
        Match match2 = new Match(new LocalSubscription(), new Subscription(), MatchStatus.SETUP_ENDPOINT);
        when(matchDiscoveryService.findMatchesByExchangeName(one.getExchangeName())).thenReturn(Arrays.asList(match1));
        when(matchDiscoveryService.findMatchesByExchangeName(two.getExchangeName())).thenReturn(Arrays.asList(match2));


        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties, matchDiscoveryService);
        collector.runSchedule();

        verify(collectorCreator,times(2)).setupCollection(any());

        assertThat(collector.getListeners()).size().isEqualTo(1);
        assertThat(collector.getListeners()).containsKeys(two);

    }

    @Test
    public void testConnectionsToNeighbourBacksOffWhenNotPossibleToContact(){
        MessageConnection messageConnectionOne = mock(MessageConnection.class);
        when(messageConnectionOne.canBeContacted(any())).thenReturn(true);

        MessageConnection messageConnectionTwo = mock(MessageConnection.class);
        when(messageConnectionTwo.canBeContacted(any())).thenReturn(true);

        ListenerEndpoint one = new ListenerEndpoint("one", "source-one", "endpoint-one", 5671,  messageConnectionOne, "subscriptionExchange1");
        ListenerEndpoint two = new ListenerEndpoint("two", "source-two", "endpoint-two", 5671,  messageConnectionTwo, "subscriptionExchange2");

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(one, two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(eq(one))).thenThrow(new MessageCollectorException("Expected exception"));
        when(collectorCreator.setupCollection(eq(two))).thenReturn(mock(MessageCollectorListener.class));

        MatchRepository matchRepository = mock(MatchRepository.class);
        MatchDiscoveryService matchDiscoveryService = new MatchDiscoveryService(matchRepository);
        when(matchDiscoveryService.findMatchesByExchangeName(one.getExchangeName())).thenReturn(Arrays.asList(new Match(new LocalSubscription(), new Subscription(), MatchStatus.SETUP_ENDPOINT)));
        when(matchDiscoveryService.findMatchesByExchangeName(two.getExchangeName())).thenReturn(Arrays.asList(new Match(new LocalSubscription(), new Subscription(), MatchStatus.SETUP_ENDPOINT)));


        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties, matchDiscoveryService);
        collector.runSchedule();

        verify(messageConnectionOne,times(1)).failedMessageConnection(anyInt());
        verify(messageConnectionTwo,times(1)).okMessageConnection();
    }

    @Test
    public void testTwoListenersAreCreatedForOneNeighbourWithTwoEndpoints() {
        MessageConnection messageConnectionOne = mock(MessageConnection.class);
        when(messageConnectionOne.canBeContacted(any())).thenReturn(true);

        MessageConnection messageConnectionTwo = mock(MessageConnection.class);
        when(messageConnectionTwo.canBeContacted(any())).thenReturn(true);

        ListenerEndpoint one = new ListenerEndpoint("one", "source-one", "endpoint-one", 5671, messageConnectionOne, "subscriptionExchange1");
        ListenerEndpoint two = new ListenerEndpoint("one", "source-two", "endpoint-two", 5671, messageConnectionTwo, "subscriptionExchange2");

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(one, two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(eq(one))).thenReturn(mock(MessageCollectorListener.class));
        when(collectorCreator.setupCollection(eq(two))).thenReturn(mock(MessageCollectorListener.class));

        MatchRepository matchRepository = mock(MatchRepository.class);
        MatchDiscoveryService matchDiscoveryService = new MatchDiscoveryService(matchRepository);
        when(matchDiscoveryService.findMatchesByExchangeName(one.getExchangeName())).thenReturn(Arrays.asList(new Match(new LocalSubscription(), new Subscription(), MatchStatus.SETUP_ENDPOINT)));
        when(matchDiscoveryService.findMatchesByExchangeName(two.getExchangeName())).thenReturn(Arrays.asList(new Match(new LocalSubscription(), new Subscription(), MatchStatus.SETUP_ENDPOINT)));

        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties, matchDiscoveryService);
        collector.runSchedule();

        assertThat(collector.getListeners()).size().isEqualTo(2);
        verify(messageConnectionOne,times(1)).okMessageConnection();
        verify(messageConnectionTwo,times(1)).okMessageConnection();
    }

    @Test
    public void testOneListenerIsCreatedForOneNeighbourWithTwoEndpointsOneFailing() {
        MessageConnection messageConnectionOne = mock(MessageConnection.class);
        when(messageConnectionOne.canBeContacted(any())).thenReturn(true);

        MessageConnection messageConnectionTwo = mock(MessageConnection.class);
        when(messageConnectionTwo.canBeContacted(any())).thenReturn(true);

        ListenerEndpoint one = new ListenerEndpoint("one", "source-one", "endpoint-one", 5671, messageConnectionOne, "subscriptionExchange1");
        ListenerEndpoint two = new ListenerEndpoint("one", "source-two", "endpoint-two", 5671, messageConnectionTwo, "subscriptionExchange2");

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(one, two));
        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(eq(one))).thenThrow(new MessageCollectorException("Expected exception"));
        when(collectorCreator.setupCollection(eq(two))).thenReturn(mock(MessageCollectorListener.class));

        MatchRepository matchRepository = mock(MatchRepository.class);
        MatchDiscoveryService matchDiscoveryService = new MatchDiscoveryService(matchRepository);
        when(matchDiscoveryService.findMatchesByExchangeName(one.getExchangeName())).thenReturn(Arrays.asList(new Match(new LocalSubscription(), new Subscription(), MatchStatus.SETUP_ENDPOINT)));
        when(matchDiscoveryService.findMatchesByExchangeName(two.getExchangeName())).thenReturn(Arrays.asList(new Match(new LocalSubscription(), new Subscription(), MatchStatus.SETUP_ENDPOINT)));

        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties, matchDiscoveryService);
        collector.runSchedule();

        assertThat(collector.getListeners()).size().isEqualTo(1);
        verify(messageConnectionOne,times(1)).failedMessageConnection(anyInt());
        verify(messageConnectionTwo,times(1)).okMessageConnection();
    }

    @Test
    public void matchWithNoListenerEndpointIsSetToTearDownExchange() {
        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Collections.emptyList());

        MatchRepository matchRepository = mock(MatchRepository.class);
        MatchDiscoveryService matchDiscoveryService = new MatchDiscoveryService(matchRepository);

        Match match = new Match(new LocalSubscription(), new Subscription(), MatchStatus.TEARDOWN_ENDPOINT);
        when(matchDiscoveryService.findMatchesToTearDownEndpointsFor()).thenReturn(Collections.singletonList(match));

        CollectorCreator collectorCreator = mock(CollectorCreator.class);

        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties, matchDiscoveryService);
        collector.runSchedule();

        assertThat(collector.getListeners()).size().isEqualTo(0);
        assertThat(match.getStatus()).isEqualTo(MatchStatus.TEARDOWN_EXCHANGE);
    }

    @Test
    public void twoMatchesOneWithListenerEndpointAndOneWithoutTearDownOne() {
        MessageConnection messageConnectionOne = mock(MessageConnection.class);
        when(messageConnectionOne.canBeContacted(any())).thenReturn(true);

        ListenerEndpoint one = new ListenerEndpoint("one", "source-one", "endpoint-one", 5671, messageConnectionOne, "subscriptionExchange1");

        GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

        ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
        when(listenerEndpointRepository.findAll()).thenReturn(Collections.singletonList(one));

        CollectorCreator collectorCreator = mock(CollectorCreator.class);
        when(collectorCreator.setupCollection(eq(one))).thenReturn(mock(MessageCollectorListener.class));

        MatchRepository matchRepository = mock(MatchRepository.class);
        MatchDiscoveryService matchDiscoveryService = new MatchDiscoveryService(matchRepository);

        Match match1 = new Match(new LocalSubscription(), new Subscription(), MatchStatus.TEARDOWN_ENDPOINT);
        Match match2 = new Match(new LocalSubscription(), new Subscription(), MatchStatus.SETUP_ENDPOINT);
        when(matchDiscoveryService.findMatchesToTearDownEndpointsFor()).thenReturn(Collections.singletonList(match1));
        when(matchDiscoveryService.findMatchesByExchangeName(one.getExchangeName())).thenReturn(Arrays.asList(match2));


        MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties, matchDiscoveryService);
        collector.runSchedule();

        assertThat(collector.getListeners()).size().isEqualTo(1);
        assertThat(match1.getStatus()).isEqualTo(MatchStatus.TEARDOWN_EXCHANGE);
        assertThat(match2.getStatus()).isEqualTo(MatchStatus.UP);
        verify(messageConnectionOne,times(1)).okMessageConnection();
    }
}
