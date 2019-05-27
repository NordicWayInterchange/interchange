package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.model.Interchange;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterchangeRepository extends CrudRepository<Interchange, Integer> {

	@Query(value = "select * from interchanges where name=?1", nativeQuery = true)
	Interchange findByName(String name);


	// Subscription polling: all interchanges with subscriptions in fedIn with status REQUESTED or ACCEPTED
	@Query(value = "select * from interchanges where ixn_id_fed_in in (select subreq_id_sub from subscriptions where subscription_status = 'REQUESTED' or subscription_status ='ACCEPTED')", nativeQuery = true)
	List<Interchange> findInterchangesWithSubscriptionToPoll();

	// Selectors for capability and subscription exchange
	// Capability exchange: when neighbour capabilities is UNKNOWN
	@Query(value = "select * from interchanges where ixn_id_cap in (select cap_id from capabilities where status = 'UNKNOWN')", nativeQuery = true)
	List<Interchange> findInterchangesForCapabilityExchange();

	// Subscription request: When neighbour capabilities is KNOWN and neighbour fedIn is EMPTY
	@Query(value = "select * from interchanges where ixn_id_cap in (select cap_id from capabilities where status = 'KNOWN') intersect select * from interchanges where ixn_id_fed_in in (select subreq_id from subscription_request where status = 'EMPTY')", nativeQuery = true)
	List<Interchange> findInterchangesForSubscriptionRequest();


	// Selectors for graceful backoff
	@Query(value = "select * from interchanges where ixn_id_fed_in in (select subreq_id from subscription_request where status ='FAILED')", nativeQuery = true)
	List<Interchange> findInterchangesWithFailedFedIn();

	@Query(value = "select * from interchanges where ixn_id_cap in (select cap_id from capabilities where status = 'FAILED')", nativeQuery = true)
	List<Interchange> findInterchangesWithFailedCapabilityExchange();

	@Query(value = "select * from interchanges where ixn_id_fed_in in (select subreq_id_sub from subscriptions where subscription_status = 'FAILED')", nativeQuery = true)
	List<Interchange> findInterchangesWithFailedSubscriptionsInFedIn();


	// Selectors for subscription set up and tear down
	@Query(value = "select * from interchanges i where exists (select 'x' from subscription_request sr where i.ixn_id_sub_out = sr.subreq_id and sr.status = 'REQUESTED' and exists(select 'x' from subscriptions s where sr.subreq_id = s.subreq_id_sub and s.subscription_status = 'ACCEPTED'))", nativeQuery = true)
	List<Interchange> findInterchangesForOutgoingSubscriptionSetup();

	@Query(value = "select * from interchanges i where exists (select 'x' from subscription_request sr where i.ixn_id_sub_out = sr.subreq_id and sr.status = 'TEAR_DOWN')", nativeQuery = true)
	List<Interchange> findInterchangesForOutgoingSubscriptionTearDown();



}
