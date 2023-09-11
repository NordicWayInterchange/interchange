package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.ConnectionStatus;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListenerEndpointRepository extends CrudRepository<ListenerEndpoint, Integer> {

    List<ListenerEndpoint> findAll();

    ListenerEndpoint findByTargetAndAndSourceAndNeighbourName(String target, String source, String neighbourName);
}

