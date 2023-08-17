package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.Connection;
import no.vegvesen.ixn.federation.model.ConnectionStatus;
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
    public void findListenerEndpointByHostAndPortAndSourceAndNeighbourName() {
        ListenerEndpoint end1 = new ListenerEndpoint("neigh1", "neigh_source1", "host-1", 5671, new Connection(), "target");
        ListenerEndpoint end2 = new ListenerEndpoint("neigh2", "neigh_source2", "host-2", 5671, new Connection(), "target");
        repository.save(end1);
        repository.save(end2);

        ListenerEndpoint newEnd = repository.findByTargetAndAndSourceAndNeighbourName("target","neigh_source2", "neigh2");
        assertThat(newEnd.getNeighbourName()).isEqualTo(end2.getNeighbourName());
        assertThat(newEnd.getHost()).isEqualTo(end2.getHost());
        assertThat(newEnd.getPort()).isEqualTo(end2.getPort());
        assertThat(newEnd.getSource()).isEqualTo(end2.getSource());
    }

    @Test
    public void noMatchListenerEndpointFoundByTargetAndSourceAndNeighbourName() {
        ListenerEndpoint end3 = new ListenerEndpoint("neigh4", "neigh_source3", "host-3", 5671, new Connection(), "target");
        ListenerEndpoint end4 = new ListenerEndpoint("neigh4", "neigh_source4", "host-4", 5671, new Connection(), "target");
        repository.save(end3);
        repository.save(end4);

        ListenerEndpoint newEnd = repository.findByTargetAndAndSourceAndNeighbourName("target","neigh_source3", "neigh3");
        assertThat(newEnd).isNull();
    }

    @Test
    public void failWhenListenerEndpointsHasTheSameUniqueConstraints() {
        ListenerEndpoint end5 = new ListenerEndpoint("neigh5", "neigh_source5", "host-5", 5671, new Connection(), "target");
        ListenerEndpoint end6 = new ListenerEndpoint("neigh5", "neigh_source5", "host-5", 5671, new Connection(), "target");
        repository.save(end5);
        assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(() ->
                repository.save(end6));
    }
}
