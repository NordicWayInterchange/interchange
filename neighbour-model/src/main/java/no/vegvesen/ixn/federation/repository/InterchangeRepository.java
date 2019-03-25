package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.model.Interchange;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface InterchangeRepository extends CrudRepository<Interchange, Integer> {

	@Query(value = "select * from INTERCHANGES where name=?1", nativeQuery = true)
	Interchange findByName(String name);

	@Query(value = "select * from interchanges where last_updated between ?1 and ?2",  nativeQuery = true)
	List<Interchange> findInterchangeOlderThan(Timestamp then, Timestamp now);

	@Query(value = "select * from interchanges where ixn_id in (select ixn_id_cap from data_types where last_updated between ?1 and ?2)", nativeQuery=true)
	List<Interchange> findInterchangesWithRecentCapabilityChanges(Timestamp then, Timestamp now);

	@Query(value = "select * from interchanges where interchange_status='NEW'", nativeQuery = true)
	List<Interchange> findInterchangesWithStatusNEW();

}
