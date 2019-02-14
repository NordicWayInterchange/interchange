package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.Model.Interchange;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface InterchangeRepository extends CrudRepository<Interchange, Integer> {

	String FIND_INTERCHANGE = "select * from INTERCHANGES where ixn_id=?1";

	@Query(value = FIND_INTERCHANGE, nativeQuery = true)
	Interchange findByInterchangeId(String interchangeId);

}
