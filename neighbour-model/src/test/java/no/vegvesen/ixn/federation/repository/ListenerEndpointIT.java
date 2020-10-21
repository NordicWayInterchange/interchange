package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.Connection;
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
        ListenerEndpoint end = new ListenerEndpoint("neigh", "neigh_broker", "neigh_queue", new Connection(), "", "");
        repository.save(end);
        List<ListenerEndpoint> neighbourList = repository.findAllByNeighbourName("neigh");
        assertThat(neighbourList).isNotNull();
        assertThat(neighbourList.get(0).getNeighbourName()).isEqualTo(end.getNeighbourName());
    }

    @Test
    public void findListenerEndpointByBrokerUrlAndQueueAndNeighbourName() {
        ListenerEndpoint end1 = new ListenerEndpoint("neigh1", "neigh_broker1", "neigh_queue1", new Connection(), "", "");
        ListenerEndpoint end2 = new ListenerEndpoint("neigh2", "neigh_broker2", "neigh_queue2", new Connection(), "", "");
        repository.save(end1);
        repository.save(end2);

        ListenerEndpoint newEnd = repository.findByNeighbourNameAndBrokerUrlAndQueue("neigh2", "neigh_broker2", "neigh_queue2");
        assertThat(newEnd.getNeighbourName()).isEqualTo(end2.getNeighbourName());
        assertThat(newEnd.getBrokerUrl()).isEqualTo(end2.getBrokerUrl());
        assertThat(newEnd.getQueue()).isEqualTo(end2.getQueue());
    }

    @Test
    public void noMatchListenerEndpointFoundByBrokerUrlAndQueueAndNeighbourName() {
        ListenerEndpoint end3 = new ListenerEndpoint("neigh4", "neigh_broker3", "neigh_queue3", new Connection(), "", "");
        ListenerEndpoint end4 = new ListenerEndpoint("neigh4", "neigh_broker4", "neigh_queue4", new Connection(), "", "");
        repository.save(end3);
        repository.save(end4);

        ListenerEndpoint newEnd = repository.findByNeighbourNameAndBrokerUrlAndQueue("neigh3","neigh_broker4","neigh_queue3");
        assertThat(newEnd).isNull();
    }

    @Test
    public void failWhenListenerEndpointsHasTheSameUniqueConstraints() {
        ListenerEndpoint end5 = new ListenerEndpoint("neigh5", "neigh_broker5", "neigh_queue5", new Connection(), "", "");
        ListenerEndpoint end6 = new ListenerEndpoint("neigh5", "neigh_broker5", "neigh_queue5", new Connection(), "", "");
        repository.save(end5);
        assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(() ->
                repository.save(end6));
    }
}
