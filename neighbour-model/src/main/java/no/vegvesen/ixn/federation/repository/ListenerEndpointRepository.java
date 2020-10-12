package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListenerEndpointRepository extends CrudRepository<ListenerEndpoint, Integer> {

    ListenerEndpoint findByNeighbourName (String neighbourName);

    ListenerEndpoint findByBrokerUrlAndQueue (String brokerUrl, String queue);

    ListenerEndpoint findByNeighbourNameAndBrokerUrlAndQueue (String neighbourName, String brokerUrl, String queue);

    //List<ListenerEndpoint> findAllByBrokerUrl_And_Queue( String brokerUrl, String queue );

    //List<ListenerEndpoint> findAllByBrokerUrl_And_Queue_And_NeighbourName( String brokerUrl, String queue, String neighbourName);
}

