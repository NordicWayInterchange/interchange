package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.model.Interchange;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterchangeRepository extends CrudRepository<Interchange, Integer> {

	@Query(value = "select * from INTERCHANGES where name=?1", nativeQuery = true)
	Interchange findByName(String name);


	// Find interchanges with fedIn status requested; Poll for subscription status
	@Query(value = "select * from interchanges where ixn_id_fed_in in(select subreq_id from subscription_request where status='REQUESTED')", nativeQuery = true)
	List<Interchange> findInterchangesToPollForSubscriptionStatus();

	// Selectors for capability and subscription exchange
	@Query(value = "select * from interchanges where ixn_id_cap in (select cap_id from capabilities where status = 'UNKNOWN')", nativeQuery = true)
	List<Interchange> findInterchangesForCapabilityExchange();

	@Query(value = "select * from interchanges where ixn_id_cap in (select cap_id from capabilities where status = 'KNOWN') intersect select * from interchanges where ixn_id_sub_out in (select subreq_id from subscription_request where status = 'EMPTY')", nativeQuery = true)
	List<Interchange> findInterchangesForSubscriptionRequest();


	// Selectors for graceful backoff
	@Query(value = "select * from interchanges where ixn_id_fed_in in (select subreq_id from subscription_request where status ='FAILED')", nativeQuery = true)
	List<Interchange> findInterchangesWithFailedFedIn();

	@Query(value = "select * from interchanges where ixn_id_cap in (select cap_id from capabilities where status = 'FAILED')", nativeQuery = true)
	List<Interchange> findInterchangesWithFailedCapabilityExchange();



}
