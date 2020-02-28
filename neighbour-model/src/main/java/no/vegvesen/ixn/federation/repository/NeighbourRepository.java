package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeighbourRepository extends CrudRepository<Neighbour, Integer> {

	Neighbour findByName(String name);

	List<Neighbour> findAll();

	List<Neighbour> findByCapabilities_Status(Capabilities.CapabilitiesStatus capabilitiesStatus);

	List<Neighbour> findBySubscriptionRequest_Status(SubscriptionRequestStatus status);

	List<Neighbour> findByFedIn_StatusIn(SubscriptionRequestStatus... statuses);

	List<Neighbour> findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus... subscriptionStatus);

	List<Neighbour> findNeighboursByCapabilities_Status_AndFedIn_Status(Capabilities.CapabilitiesStatus capabilitiesStatus, SubscriptionRequestStatus requestStatus);

	@Query(value = "select distinct i from Neighbour i join i.subscriptionRequest sr join sr.subscription s where sr.status = :subscriptionRequestStatus and s.subscriptionStatus = :subscriptionStatus")
	List<Neighbour> findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(
			@Param("subscriptionRequestStatus") SubscriptionRequestStatus subscriptionRequestStatus,
			@Param("subscriptionStatus") SubscriptionStatus subscriptionStatus
	);

}
