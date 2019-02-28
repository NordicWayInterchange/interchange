package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.model.Interchange;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface InterchangeRepository extends CrudRepository<Interchange, Integer> {

	String FIND_INTERCHANGE = "select * from INTERCHANGES where name=?1";

	@Query(value = FIND_INTERCHANGE, nativeQuery = true)
	Interchange findByName(String name);

	String FIND_OLDER_THAN = "select * from INTERCHANGES where last_updated between ?1 and ?2";

	@Query(value = "SELECT * FROM INTERCHANGES WHERE last_updated BETWEEN ?1 AND ?2",  nativeQuery = true)
	List<Interchange> findOlderThan(Timestamp then, Timestamp now);

}
