package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.DiscoveryState;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscoveryStateRepository extends CrudRepository<DiscoveryState, Integer> {

	DiscoveryState findByName(String name);
}
