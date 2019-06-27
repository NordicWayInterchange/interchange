package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterchangeRepository extends CrudRepository<Interchange, Integer> {

	Interchange findByName(String name);

	List<Interchange> findByCapabilities_Status(Capabilities.CapabilitiesStatus capabilitiesStatus);

	List<Interchange> findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus status);

	List<Interchange> findByFedIn_Status(SubscriptionRequest.SubscriptionRequestStatus status);

	List<Interchange> findInterchangesByFedIn_Subscription_SubscriptionStatus(Subscription.SubscriptionStatus subscriptionStatus);

	List<Interchange> findInterchangesByCapabilities_Status_AndFedIn_Status(Capabilities.CapabilitiesStatus capabilitiesStatus, SubscriptionRequest.SubscriptionRequestStatus requestStatus);

	// Neighbours to add to qpid groups file: fedIn status REQUESTED or ESTABLISHED
	@Query(value ="select * from interchanges i where exists(select 'x' from subscription_request s where i.ixn_id_fed_in=s.subreq_id and s.status in('REQUESTED', 'ESTABLISHED'))", nativeQuery = true)
	List<Interchange> findInterchangesToAddToQpidGroups();

	// Neighbours to remove from qpid groups file: fedIn status REJECTED
	@Query(value = "select * from interchanges i where exists(select 'x' from subscription_request s where i.ixn_id_fed_in=s.subreq_id and s.status='REJECTED')", nativeQuery = true)
	List<Interchange> findInterchangesToRemoveFromQpidGroups();

	// Selectors for subscription set up and tear down
	@Query(value = "select distinct i from Interchange i join i.subscriptionRequest sr join sr.subscription s where sr.status = 'REQUESTED' and s.subscriptionStatus = 'ACCEPTED'")
	List<Interchange> findInterchangesForOutgoingSubscriptionSetup();
}
