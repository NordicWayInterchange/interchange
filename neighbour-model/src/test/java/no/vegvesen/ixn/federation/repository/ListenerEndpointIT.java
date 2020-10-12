package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;


@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class ListenerEndpointIT {

    //@MockBean
    //Connection mockConnection;

    @Autowired
    private ListenerEndpointRepository repository;

    @Test
    public void findListenerEndpointByNeighbourName() {
        ListenerEndpoint end = new ListenerEndpoint("neigh", "neigh_broker", "neigh_queue");
        repository.save(end);
        System.out.println(end.getNeighbourName());
        ListenerEndpoint end1 = repository.findByNeighbourName("neigh");
        System.out.println(end1.getNeighbourName());
        //System.out.println(repository.findByNeighbourName("neigh"));
    }

    @Test
    public void findListenerEndpointByBrokerUrlAndQueue() {}

    @Test
    public void findListenerEndpointByBrokerUrlAndQueueAndNeighbourName() {}
}
