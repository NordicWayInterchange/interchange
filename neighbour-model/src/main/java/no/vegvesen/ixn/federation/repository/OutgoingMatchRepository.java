package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.OutgoingMatch;
import no.vegvesen.ixn.federation.model.OutgoingMatchStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutgoingMatchRepository extends CrudRepository<OutgoingMatch, Integer> {

    List<OutgoingMatch> findAll();

    List<OutgoingMatch> findAllByStatus(OutgoingMatchStatus status);

    List<OutgoingMatch> findAllByServiceProviderNameAndStatus(String serviceProviderName, OutgoingMatchStatus status);
}
