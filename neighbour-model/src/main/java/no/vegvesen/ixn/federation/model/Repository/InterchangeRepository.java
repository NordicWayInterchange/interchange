package no.vegvesen.ixn.federation.model.Repository;


import no.vegvesen.ixn.federation.model.Model.Interchange;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterchangeRepository extends CrudRepository<Interchange, Integer> {

	String FIND_INTERCHANGE = "select * from INTERCHANGES where name=?1";

	@Query(value = FIND_INTERCHANGE, nativeQuery = true)
	Interchange findByInterchangeId(String interchangeId);

	String FIND_OLDER_THAN = "select * from INTERCHANGES where last_updated<?1";

	@Query(value = FIND_OLDER_THAN, nativeQuery = true)
	List<Interchange> findOlderThan(String timestamp);

}
