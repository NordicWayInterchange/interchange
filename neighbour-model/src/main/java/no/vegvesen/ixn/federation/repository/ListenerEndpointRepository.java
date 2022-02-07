package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.ConnectionStatus;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListenerEndpointRepository extends CrudRepository<ListenerEndpoint, Integer> {

    List<ListenerEndpoint> findAll();

    List<ListenerEndpoint> findAllByNeighbourName (String neighbourName);

    ListenerEndpoint findByNeighbourNameAndHostAndPortAndSource(String neighbourName, String host, int port, String source);

    List<ListenerEndpoint> findByMessageConnection_ConnectionStatus(ConnectionStatus connectionStatus);

    ListenerEndpoint findByExchangeName(String exchangeName);
}

