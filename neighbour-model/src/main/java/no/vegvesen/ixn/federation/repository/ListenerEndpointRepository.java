package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListenerEndpointRepository extends CrudRepository<ListenerEndpoint, Integer> {

    List<ListenerEndpoint> findAllByNeighbourName (String neighbourName);

    ListenerEndpoint findByNeighbourNameAndBrokerUrlAndQueue (String neighbourName, String brokerUrl, String queue);
}

