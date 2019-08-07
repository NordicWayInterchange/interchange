package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.model.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeighbourRepository extends CrudRepository<Neighbour, Integer> {

	Neighbour findByName(String name);

	List<Neighbour> findByCapabilities_Status(Capabilities.CapabilitiesStatus capabilitiesStatus);

	List<Neighbour> findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus status);

	List<Neighbour> findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus... statuses);

	@SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
	List<Neighbour> findInterchangesByFedIn_Subscription_SubscriptionStatusIn(Subscription.SubscriptionStatus... subscriptionStatus);

	List<Neighbour> findInterchangesByCapabilities_Status_AndFedIn_Status(Capabilities.CapabilitiesStatus capabilitiesStatus, SubscriptionRequest.SubscriptionRequestStatus requestStatus);

	@Query(value = "select distinct i from Neighbour i join i.subscriptionRequest sr join sr.subscription s where sr.status = :subscriptionRequestStatus and s.subscriptionStatus = :subscriptionStatus")
	List<Neighbour> findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(
			@Param("subscriptionRequestStatus") SubscriptionRequest.SubscriptionRequestStatus subscriptionRequestStatus,
			@Param("subscriptionStatus") Subscription.SubscriptionStatus subscriptionStatus
	);
}
