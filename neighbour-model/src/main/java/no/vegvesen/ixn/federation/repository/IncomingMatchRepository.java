package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.IncomingMatch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncomingMatchRepository extends CrudRepository<IncomingMatch, Integer> {
    IncomingMatch findByNeighbourSubscriptionId(Integer id);
    List<IncomingMatch> findAll();
}
