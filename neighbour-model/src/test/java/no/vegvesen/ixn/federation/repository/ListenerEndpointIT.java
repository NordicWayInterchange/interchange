package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.MessageConnection;
import no.vegvesen.ixn.federation.model.MessageConnectionStatus;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class ListenerEndpointIT {

    @Autowired
    private ListenerEndpointRepository repository;

    @Test
    public void findListenerEndpointByNeighbourName() {
        ListenerEndpoint end = new ListenerEndpoint("neigh", "neigh_source", "host", 5671, new MessageConnection());
        repository.save(end);
        List<ListenerEndpoint> neighbourList = repository.findAllByNeighbourName("neigh");
        assertThat(neighbourList).isNotNull();
        assertThat(neighbourList.get(0).getNeighbourName()).isEqualTo(end.getNeighbourName());
    }

    @Test
    public void findListenerEndpointByHostAndPortAndSourceAndNeighbourName() {
        ListenerEndpoint end1 = new ListenerEndpoint("neigh1", "neigh_source1", "host-1", 5671, new MessageConnection());
        ListenerEndpoint end2 = new ListenerEndpoint("neigh2", "neigh_source2", "host-2", 5671, new MessageConnection());
        repository.save(end1);
        repository.save(end2);

        ListenerEndpoint newEnd = repository.findByNeighbourNameAndHostAndPortAndSource("neigh2", "host-2", 5671, "neigh_source2");
        assertThat(newEnd.getNeighbourName()).isEqualTo(end2.getNeighbourName());
        assertThat(newEnd.getHost()).isEqualTo(end2.getHost());
        assertThat(newEnd.getPort()).isEqualTo(end2.getPort());
        assertThat(newEnd.getSource()).isEqualTo(end2.getSource());
    }

    @Test
    public void noMatchListenerEndpointFoundByHostAndPortAndSourceAndNeighbourName() {
        ListenerEndpoint end3 = new ListenerEndpoint("neigh4", "neigh_source3", "host-3", 5671, new MessageConnection());
        ListenerEndpoint end4 = new ListenerEndpoint("neigh4", "neigh_source4", "host-4", 5671, new MessageConnection());
        repository.save(end3);
        repository.save(end4);

        ListenerEndpoint newEnd = repository.findByNeighbourNameAndHostAndPortAndSource("neigh3","neigh_host4", 5671, "neigh_source3");
        assertThat(newEnd).isNull();
    }

    @Test
    public void failWhenListenerEndpointsHasTheSameUniqueConstraints() {
        ListenerEndpoint end5 = new ListenerEndpoint("neigh5", "neigh_source5", "host-5", 5671, new MessageConnection());
        ListenerEndpoint end6 = new ListenerEndpoint("neigh5", "neigh_source5", "host-5", 5671, new MessageConnection());
        repository.save(end5);
        assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(() ->
                repository.save(end6));
    }

    @Test
    public void messageConnectionStatusCanBeQueried() {
        ListenerEndpoint listenerEndpoint = new ListenerEndpoint("neighbourName","source", "host", 5671, new MessageConnection());
        listenerEndpoint.getMessageConnection().setMessageConnectionStatus(MessageConnectionStatus.UNREACHABLE);
        repository.save(listenerEndpoint);

        assertThat(repository.findByMessageConnection_MessageConnectionStatus(MessageConnectionStatus.UNREACHABLE)).contains(listenerEndpoint);
        assertThat(repository.findByMessageConnection_MessageConnectionStatus(MessageConnectionStatus.CONNECTED)).doesNotContain(listenerEndpoint);
    }

    @Test
    public void saveMultipleListenerEndpointsFromSameNeighbour(){
        ListenerEndpoint lisend1 = new ListenerEndpoint("neighbourName", "source-1", "host-1", 5671, new MessageConnection());
        ListenerEndpoint lisend2 = new ListenerEndpoint("neighbourName", "source-2", "host-2", 5671, new MessageConnection());

        repository.save(lisend1);
        repository.save(lisend2);

        assertThat(repository.findAllByNeighbourName("neighbourName")).hasSize(2);
    }
}
